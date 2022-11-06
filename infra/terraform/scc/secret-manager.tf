data "aws_secretsmanager_secret" "scc" {
  arn = data.terraform_remote_state.secret_manager.outputs.scc_secret_manager_arn
}

resource "aws_secretsmanager_secret_version" "scc_dev" {
  secret_id     = data.aws_secretsmanager_secret.scc.id
  secret_string = jsonencode(data.sops_file.secret_data.data)
}
