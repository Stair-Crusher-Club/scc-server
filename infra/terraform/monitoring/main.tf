provider "aws" {
  region = "ap-northeast-2"
}

terraform {
  backend "s3" {
    bucket = "scc-prod-tf-remote-state"
    key    = "monitoring.tfstate"
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
