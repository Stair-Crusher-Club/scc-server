provider "aws" {
}

terraform {
  required_providers {
    aws = {
      source = "hashicorp/aws"
    }
    external = {
      source = "hashicorp/external"
    }
  }

  backend "s3" {
    bucket = "scc-prod-tf-remote-state"
    key    = "k3s.oidc.tfstate"
    region = "ap-northeast-2"
  }
}

data "terraform_remote_state" "lightsail" {
  backend = "s3"
  config = {
    bucket = "scc-prod-tf-remote-state"
    key    = "k3s.tfstate"
    region = "ap-northeast-2"
  }
}

