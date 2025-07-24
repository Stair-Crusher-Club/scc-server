provider "aws" {
  region = "ap-northeast-2"
}

terraform {
  required_providers {
    aws = {
      source = "hashicorp/aws"
    }
  }

  backend "s3" {
    bucket = "scc-prod-tf-remote-state"
    key    = "scc.tfstate"
    region = "ap-northeast-2"
  }
}

data "terraform_remote_state" "oidc" {
  backend = "s3"
  config = {
    bucket = "scc-prod-tf-remote-state"
    key    = "k3s.oidc.tfstate"
    region = "ap-northeast-2"
  }
}

data "terraform_remote_state" "kms" {
  backend = "s3"
  config = {
    bucket = "scc-prod-tf-remote-state"
    key    = "kms.tfstate"
    region = "ap-northeast-2"
  }
}

data "terraform_remote_state" "ecr" {
  backend = "s3"
  config = {
    bucket = "scc-prod-tf-remote-state"
    key    = "ecr.tfstate"
    region = "ap-northeast-2"
  }
}
