output "instance_id" {
  description = "EC2 instance ID"
  value       = aws_instance.k3s.id
}

output "instance_public_ip" {
  description = "EC2 instance public IP"
  value       = var.use_elastic_ip ? aws_eip.k3s[0].public_ip : aws_instance.k3s.public_ip
}

output "instance_private_ip" {
  description = "EC2 instance private IP"
  value       = aws_instance.k3s.private_ip
}

output "security_group_id" {
  description = "K3s security group ID"
  value       = aws_security_group.k3s.id
}

output "k3s_kubeconfig_command" {
  description = "Command to get kubeconfig from K3s server"
  value       = "scp -i <your-key.pem> ec2-user@${var.use_elastic_ip ? aws_eip.k3s[0].public_ip : aws_instance.k3s.public_ip}:/etc/rancher/k3s/k3s.yaml ./kubeconfig.yaml"
}

output "ssh_command" {
  description = "SSH command to connect to K3s server"
  value       = "ssh -i <your-key.pem> ec2-user@${var.use_elastic_ip ? aws_eip.k3s[0].public_ip : aws_instance.k3s.public_ip}"
}
