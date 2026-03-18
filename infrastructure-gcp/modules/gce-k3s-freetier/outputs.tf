output "instance_id" {
  description = "GCE instance ID"
  value       = google_compute_instance.k3s.instance_id
}

output "instance_name" {
  description = "GCE instance name"
  value       = google_compute_instance.k3s.name
}

output "instance_public_ip" {
  description = "GCE instance external IP"
  value       = var.use_static_ip ? google_compute_address.k3s[0].address : google_compute_instance.k3s.network_interface[0].access_config[0].nat_ip
}

output "instance_private_ip" {
  description = "GCE instance internal IP"
  value       = google_compute_instance.k3s.network_interface[0].network_ip
}

output "service_account_email" {
  description = "K3s service account email"
  value       = google_service_account.k3s.email
}

output "k3s_kubeconfig_command" {
  description = "Command to get kubeconfig from K3s server"
  value       = "scp -i <your-key.pem> ${var.ssh_user}@${var.use_static_ip ? google_compute_address.k3s[0].address : google_compute_instance.k3s.network_interface[0].access_config[0].nat_ip}:/etc/rancher/k3s/k3s.yaml ./kubeconfig.yaml"
}

output "ssh_command" {
  description = "SSH command to connect to K3s server"
  value       = "ssh -i <your-key.pem> ${var.ssh_user}@${var.use_static_ip ? google_compute_address.k3s[0].address : google_compute_instance.k3s.network_interface[0].access_config[0].nat_ip}"
}

output "gcloud_ssh_command" {
  description = "gcloud SSH command (uses OS Login)"
  value       = "gcloud compute ssh ${google_compute_instance.k3s.name} --zone=${var.gcp_zone}"
}

# ============================================
# Service별 DB URL (VM 내부 MySQL Docker)
# ============================================
output "auth_service_db_url" {
  description = "Auth service JDBC URL"
  value       = "jdbc:mysql://localhost:3306/hamkkebu_auth?useSSL=false&serverTimezone=Asia/Seoul&characterEncoding=UTF-8"
}

output "ledger_service_db_url" {
  description = "Ledger service JDBC URL"
  value       = "jdbc:mysql://localhost:3306/hamkkebu_ledger?useSSL=false&serverTimezone=Asia/Seoul&characterEncoding=UTF-8"
}

output "transaction_service_db_url" {
  description = "Transaction service JDBC URL"
  value       = "jdbc:mysql://localhost:3306/hamkkebu_transaction?useSSL=false&serverTimezone=Asia/Seoul&characterEncoding=UTF-8"
}
