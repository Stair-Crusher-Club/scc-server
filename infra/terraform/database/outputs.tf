output "scc_rds_instance_identifier" {
  value = aws_db_instance.scc_rds.identifier
}

output "scc_rds_instance_host" {
  value = aws_db_instance.scc_rds.address
}

output "scc_rds_instance_username" {
  value = aws_db_instance.scc_rds.username
}

output "scc_rds_instance_password" {
  value = aws_db_instance.scc_rds.password
  sensitive = true
}
