provider "aws" {
  region = "ap-northeast-2"
}

terraform {
  backend "local" {
    path = "terraform.tfstate"
  }
}

