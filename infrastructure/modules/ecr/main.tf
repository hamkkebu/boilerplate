# ECR Repositories for hamkkebu services
# 프리티어: 500MB 무료 스토리지

locals {
  repositories = [
    "auth-service-backend",
    "auth-service-frontend",
    "ledger-service-backend",
    "ledger-service-frontend",
    "transaction-service-backend",
    "transaction-service-frontend"
  ]
}

# ECR Repositories
resource "aws_ecr_repository" "services" {
  for_each = toset(local.repositories)

  name                 = "${var.project_name}/${each.key}"
  image_tag_mutability = "MUTABLE"

  image_scanning_configuration {
    scan_on_push = false  # 프리티어 비용 최적화
  }

  encryption_configuration {
    encryption_type = "AES256"
  }

  tags = {
    Name        = "${var.project_name}-${each.key}"
    Project     = var.project_name
    Environment = var.environment
    ManagedBy   = "terraform"
  }
}

# Lifecycle Policy - 프리티어 500MB 제한 대응
# 최대 3개 이미지만 유지
resource "aws_ecr_lifecycle_policy" "cleanup" {
  for_each   = aws_ecr_repository.services
  repository = each.value.name

  policy = jsonencode({
    rules = [
      {
        rulePriority = 1
        description  = "Keep only last 3 images to stay within free tier"
        selection = {
          tagStatus   = "any"
          countType   = "imageCountMoreThan"
          countNumber = 3
        }
        action = {
          type = "expire"
        }
      }
    ]
  })
}

# IAM Policy for GitHub Actions to push images
resource "aws_iam_policy" "ecr_push" {
  name        = "${var.project_name}-${var.environment}-ecr-push-policy"
  description = "Policy for GitHub Actions to push images to ECR"

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "ecr:GetAuthorizationToken"
        ]
        Resource = "*"
      },
      {
        Effect = "Allow"
        Action = [
          "ecr:BatchCheckLayerAvailability",
          "ecr:GetDownloadUrlForLayer",
          "ecr:BatchGetImage",
          "ecr:PutImage",
          "ecr:InitiateLayerUpload",
          "ecr:UploadLayerPart",
          "ecr:CompleteLayerUpload"
        ]
        Resource = [for repo in aws_ecr_repository.services : repo.arn]
      }
    ]
  })

  tags = {
    Name        = "${var.project_name}-${var.environment}-ecr-push-policy"
    Project     = var.project_name
    Environment = var.environment
  }
}

# IAM User for GitHub Actions
resource "aws_iam_user" "github_actions" {
  count = var.create_github_actions_user ? 1 : 0
  name  = "${var.project_name}-${var.environment}-github-actions"

  tags = {
    Name        = "${var.project_name}-${var.environment}-github-actions"
    Project     = var.project_name
    Environment = var.environment
  }
}

resource "aws_iam_user_policy_attachment" "github_actions_ecr" {
  count      = var.create_github_actions_user ? 1 : 0
  user       = aws_iam_user.github_actions[0].name
  policy_arn = aws_iam_policy.ecr_push.arn
}
