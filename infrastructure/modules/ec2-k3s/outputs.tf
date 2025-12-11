output "instance_id" {
  description = "EC2 instance ID"
  value       = aws_instance.k3s_master.id
}

output "instance_private_ip" {
  description = "Private IP of the K3s master"
  value       = aws_instance.k3s_master.private_ip
}

output "instance_public_ip" {
  description = "Public IP (Elastic IP) of the K3s master"
  value       = aws_eip.k3s.public_ip
}

output "security_group_id" {
  description = "Security group ID for the K3s cluster"
  value       = aws_security_group.k3s.id
}

output "iam_role_arn" {
  description = "IAM role ARN for the K3s instance"
  value       = aws_iam_role.k3s.arn
}

output "argocd_url" {
  description = "ArgoCD UI URL"
  value       = "https://${aws_eip.k3s.public_ip}:30080"
}

output "ssh_command" {
  description = "SSH command to connect to the instance"
  value       = "ssh -i <your-key.pem> ec2-user@${aws_eip.k3s.public_ip}"
}

output "kubeconfig_command" {
  description = "Command to get kubeconfig from the instance"
  value       = "scp -i <your-key.pem> ec2-user@${aws_eip.k3s.public_ip}:/etc/rancher/k3s/k3s.yaml ./kubeconfig.yaml"
}
