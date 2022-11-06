provider "aws" {
  region = "ap-northeast-2"
}

terraform {
  required_providers {
    aws = {
      source = "hashicorp/aws"
    }
    sops = {
      source = "carlpett/sops"
    }
  }

  backend "s3" {
    bucket = "scc-tf-remote-state"
    key    = "scc.tfstate"
    region = "ap-northeast-2"
  }
}

data "terraform_remote_state" "oidc" {
  backend = "s3"
  config = {
    bucket = "scc-tf-remote-state"
    key    = "k3s.domain.tfstate"
    region = "ap-northeast-2"
  }
}

data "terraform_remote_state" "secret_manager" {
  backend = "s3"
  config = {
    bucket = "scc-tf-remote-state"
    key    = "secret-manager.tfstate"
    region = "ap-northeast-2"
  }
}

data "sops_file" "secret_data" {
  source_file = "secret.yaml"
}
