output "scc_server_repository_arn" {
  value = aws_ecr_repository.scc_server.arn
}

output "scc_admin_frontend_repository_arn" {
  value = aws_ecr_repository.scc_admin_frontend.arn
}
