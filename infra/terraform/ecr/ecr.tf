resource "aws_ecr_repository" "scc_server" {
  provider = aws
  name = "scc-server"

  image_scanning_configuration {
    scan_on_push = true
  }
}

resource "aws_ecr_repository" "scc_admin_frontend" {
  provider = aws
  name = "scc-admin-frontend"

  image_scanning_configuration {
    scan_on_push = true
  }
}

# Deprecated
resource "aws_ecrpublic_repository" "scc_server" {
  provider = aws.us-east-1
  repository_name = "scc-server"
  catalog_data {
    architectures     = ["ARM64", "x86-64"]
    operating_systems = ["Linux"]
  }
}

# Deprecated
resource "aws_ecrpublic_repository" "scc_admin_frontend" {
  provider = aws.us-east-1
  repository_name = "scc-admin-frontend"
  catalog_data {
    architectures     = ["ARM64", "x86-64"]
    operating_systems = ["Linux"]
  }
}

resource "aws_ecr_lifecycle_policy" "scc_server_policy" {
  repository = aws_ecr_repository.scc_server.name
  policy = local.standard_policy
}

resource "aws_ecr_lifecycle_policy" "scc_admin_frontend_policy" {
  repository = aws_ecr_repository.scc_admin_frontend.name
  policy = local.standard_policy
}

locals {
  standard_policy = jsonencode({
    rules = [
      {
        rulePriority = 1
        description = "Expire untagged images older than 3 days"
        selection = {
          tagStatus = "untagged"
          countType = "sinceImagePushed"
          countUnit = "days"
          countNumber = 3
        }
        action = {
          type = "expire"
        }
      },
      {
        rulePriority = 2
        description = "Expire any image older than 365 days (1 year)"
        selection = {
          tagStatus = "any"
          countType = "sinceImagePushed"
          countUnit = "days"
          countNumber = 365
        }
        action = {
          type = "expire"
        }
      }
    ]
  })
}
