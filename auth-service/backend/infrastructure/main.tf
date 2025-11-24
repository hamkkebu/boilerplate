terraform {
  required_version = ">= 1.5.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }

  backend "s3" {
    key    = "services/auth-service/terraform.tfstate"
    region = "ap-northeast-2"
    encrypt = true
    dynamodb_table = "terraform-state-lock"
  }
}

provider "aws" {
  region = var.aws_region
}

# Data sources to reference common infrastructure
data "terraform_remote_state" "common" {
  backend = "s3"
  config = {
    bucket = var.terraform_state_bucket
    key    = "common/terraform.tfstate"
    region = var.aws_region
  }
}

# IAM Role for ECS Task Execution
resource "aws_iam_role" "ecs_task_execution" {
  name = "${var.project_name}-${var.environment}-auth-service-task-execution-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Action = "sts:AssumeRole"
      Effect = "Allow"
      Principal = {
        Service = "ecs-tasks.amazonaws.com"
      }
    }]
  })
}

resource "aws_iam_role_policy_attachment" "ecs_task_execution" {
  role       = aws_iam_role.ecs_task_execution.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}

# IAM Role for ECS Task
resource "aws_iam_role" "ecs_task" {
  name = "${var.project_name}-${var.environment}-auth-service-task-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Action = "sts:AssumeRole"
      Effect = "Allow"
      Principal = {
        Service = "ecs-tasks.amazonaws.com"
      }
    }]
  })
}

# CloudWatch Log Group for auth-service
resource "aws_cloudwatch_log_group" "auth_service" {
  name              = "/ecs/${var.project_name}-${var.environment}/auth-service"
  retention_in_days = 7

  tags = {
    Name        = "${var.project_name}-${var.environment}-auth-service-logs"
    Service     = "auth-service"
    Environment = var.environment
  }
}

# ECS Task Definition
resource "aws_ecs_task_definition" "auth_service" {
  family                   = "${var.project_name}-${var.environment}-auth-service"
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = var.task_cpu
  memory                   = var.task_memory
  execution_role_arn       = aws_iam_role.ecs_task_execution.arn
  task_role_arn            = aws_iam_role.ecs_task.arn

  container_definitions = jsonencode([{
    name  = "auth-service"
    image = "${var.ecr_repository_url}:${var.image_tag}"

    portMappings = [{
      containerPort = var.container_port
      protocol      = "tcp"
    }]

    environment = [
      {
        name  = "SPRING_PROFILES_ACTIVE"
        value = var.environment
      },
      {
        name  = "SERVER_PORT"
        value = tostring(var.container_port)
      },
      {
        name  = "DB_HOST"
        value = data.terraform_remote_state.common.outputs.db_endpoint
      },
      {
        name  = "DB_PORT"
        value = tostring(data.terraform_remote_state.common.outputs.db_port)
      },
      {
        name  = "DB_NAME"
        value = "hamkkebu_auth"
      }
    ]

    secrets = [
      {
        name      = "DB_USERNAME"
        valueFrom = "${aws_secretsmanager_secret.db_credentials.arn}:username::"
      },
      {
        name      = "DB_PASSWORD"
        valueFrom = "${aws_secretsmanager_secret.db_credentials.arn}:password::"
      }
    ]

    logConfiguration = {
      logDriver = "awslogs"
      options = {
        "awslogs-group"         = aws_cloudwatch_log_group.auth_service.name
        "awslogs-region"        = var.aws_region
        "awslogs-stream-prefix" = "ecs"
      }
    }

    healthCheck = {
      command     = ["CMD-SHELL", "curl -f http://localhost:${var.container_port}/api/users/health || exit 1"]
      interval    = 30
      timeout     = 5
      retries     = 3
      startPeriod = 60
    }
  }])

  tags = {
    Name        = "${var.project_name}-${var.environment}-auth-service"
    Service     = "auth-service"
    Environment = var.environment
  }
}

# Secrets Manager for database credentials
resource "aws_secretsmanager_secret" "db_credentials" {
  name = "${var.project_name}/${var.environment}/auth-service/db-credentials"

  tags = {
    Name        = "${var.project_name}-${var.environment}-auth-service-db-credentials"
    Service     = "auth-service"
    Environment = var.environment
  }
}

# ALB Target Group for auth-service
resource "aws_lb_target_group" "auth_service" {
  name        = "${var.project_name}-${var.environment}-auth-svc-tg"
  port        = var.container_port
  protocol    = "HTTP"
  vpc_id      = data.terraform_remote_state.common.outputs.vpc_id
  target_type = "ip"

  health_check {
    enabled             = true
    healthy_threshold   = 2
    unhealthy_threshold = 3
    timeout             = 5
    interval            = 30
    path                = "/api/users/health"
    matcher             = "200"
  }

  deregistration_delay = 30

  tags = {
    Name        = "${var.project_name}-${var.environment}-auth-service-tg"
    Service     = "auth-service"
    Environment = var.environment
  }
}

# ALB Listener Rule for auth-service
resource "aws_lb_listener_rule" "auth_service" {
  listener_arn = data.terraform_remote_state.common.outputs.alb_listener_arn

  action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.auth_service.arn
  }

  condition {
    path_pattern {
      values = ["/api/auth/*", "/api/users/*"]
    }
  }

  tags = {
    Name        = "${var.project_name}-${var.environment}-auth-service-rule"
    Service     = "auth-service"
    Environment = var.environment
  }
}

# ECS Service
resource "aws_ecs_service" "auth_service" {
  name            = "${var.project_name}-${var.environment}-auth-service"
  cluster         = data.terraform_remote_state.common.outputs.ecs_cluster_id
  task_definition = aws_ecs_task_definition.auth_service.arn
  desired_count   = var.desired_count
  launch_type     = "FARGATE"

  network_configuration {
    subnets          = data.terraform_remote_state.common.outputs.private_subnet_ids
    security_groups  = [data.terraform_remote_state.common.outputs.ecs_security_group_id]
    assign_public_ip = false
  }

  load_balancer {
    target_group_arn = aws_lb_target_group.auth_service.arn
    container_name   = "auth-service"
    container_port   = var.container_port
  }

  depends_on = [aws_lb_listener_rule.auth_service]

  tags = {
    Name        = "${var.project_name}-${var.environment}-auth-service"
    Service     = "auth-service"
    Environment = var.environment
  }
}

# Auto Scaling
resource "aws_appautoscaling_target" "auth_service" {
  max_capacity       = var.max_capacity
  min_capacity       = var.min_capacity
  resource_id        = "service/${data.terraform_remote_state.common.outputs.ecs_cluster_name}/${aws_ecs_service.auth_service.name}"
  scalable_dimension = "ecs:service:DesiredCount"
  service_namespace  = "ecs"
}

resource "aws_appautoscaling_policy" "auth_service_cpu" {
  name               = "${var.project_name}-${var.environment}-auth-service-cpu-scaling"
  policy_type        = "TargetTrackingScaling"
  resource_id        = aws_appautoscaling_target.auth_service.resource_id
  scalable_dimension = aws_appautoscaling_target.auth_service.scalable_dimension
  service_namespace  = aws_appautoscaling_target.auth_service.service_namespace

  target_tracking_scaling_policy_configuration {
    target_value = 70.0

    predefined_metric_specification {
      predefined_metric_type = "ECSServiceAverageCPUUtilization"
    }

    scale_in_cooldown  = 300
    scale_out_cooldown = 60
  }
}
