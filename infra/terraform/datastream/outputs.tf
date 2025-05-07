output "datastream_user_password" {
  value     = random_password.scc_rds_datastream_user_password.result
  sensitive = true
}
