provider "aws" {
  region = "us-east-1"
}

terraform {
  backend "s3" {
    bucket = "scc-tf-remote-state"
    key    = "lightsail.domain.tfstate"
    region = "ap-northeast-2"
  }
}

data "terraform_remote_state" "lightsail" {
  backend = "s3"
  config = {
    bucket = "scc-tf-remote-state"
    key    = "lightsail.tfstate"
    region = "ap-northeast-2"
  }
}

