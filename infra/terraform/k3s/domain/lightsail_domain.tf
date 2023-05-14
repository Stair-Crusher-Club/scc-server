resource "aws_lightsail_domain" "staircrusher_club" {
  domain_name = "staircrusher.club"
}

resource "aws_lightsail_domain" "dev_staircrusher_club" {
  domain_name = "dev.staircrusher.club"
}

# FIXME: does not work correctly due to terraform bug
# resource "aws_lightsail_domain_entry" "acm" {
#   for_each = {
#     "_2467817f8161e4f737c5380cac02ca88.api"       = "_abfc53231f4a4cb78039f67b0f25f814.bkngfjypgb.acm-validations.aws.",
#     "_68805ce864e49431a924f853a0caae1b.admin-api" = "_66d4049354143869bf0a210467ebff13.bkngfjypgb.acm-validations.aws.",
#     "_cad568561e5d03fbee1bd4dcc6281ca6"           = "_11d889bfc325f00638a4628718dd5b2a.bkngfjypgb.acm-validations.aws.",
#     "_e0533ee0a07b2235970940e292a911c6.www"       = "_dd7ff4f50e586c1a33a529b08877c5ec.bkngfjypgb.acm-validations.aws.",
#     "_9a2c8af390022ee0a73338b2deb8d01f.admin"     = "_3c3e1e86745a9f4315d4208669612f41.yzdtlljtvc.acm-validations.aws.",
#   }
#   domain_name = aws_lightsail_domain.staircrusher_club.domain_name
#   type        = "CNAME"
#   name        = each.key
#   target      = each.value
# }

# resource "aws_lightsail_domain_entry" "dev_acm" {
#   for_each = {
#     "_de9c3da5ed9a0b9e821cdbbb336910c4.admin-api" = "_65f80983cd357f10562cd0d7ea83a8c3.bkngfjypgb.acm-validations.aws.",
#     "_898e1443bdf6c0a1d604ae60a18c60d2.admin"     = "_0f81388ab4bff9a8578352eaf9a02467.crxktfrmng.acm-validations.aws.",
#     "_6d98a5c1c057035b857773878a2a571d.api"       = "_abc1751100762fc6678e0b6ffff7cb7b.bkngfjypgb.acm-validations.aws.",
#   }
#   domain_name = aws_lightsail_domain.dev_staircrusher_club.domain_name
#   type        = "CNAME"
#   name        = each.key
#   target      = each.value
# }

resource "aws_lightsail_domain_entry" "dev_ns" {
  for_each = toset([
    "ns-1217.awsdns-24.org",
    "ns-532.awsdns-02.net",
    "ns-1753.awsdns-27.co.uk",
    "ns-51.awsdns-06.com",
  ])
  domain_name = aws_lightsail_domain.staircrusher_club.domain_name
  name        = "dev"
  type        = "NS"
  target      = each.key
}

# FIXME: this entry should be created before creating oidc connector
resource "aws_lightsail_domain_entry" "k3s" {
  domain_name = aws_lightsail_domain.staircrusher_club.domain_name
  name        = "k3s"
  type        = "A"
  target      = data.terraform_remote_state.lightsail.outputs.k3s_ip
}

resource "aws_lightsail_domain_entry" "api" {
  domain_name = aws_lightsail_domain.staircrusher_club.domain_name
  name        = "api"
  type        = "A"
  target      = data.terraform_remote_state.lightsail.outputs.lb_dns_name
  is_alias    = true

  lifecycle {
    create_before_destroy = false
  }
}

resource "aws_lightsail_domain_entry" "dev_api" {
  domain_name = aws_lightsail_domain.dev_staircrusher_club.domain_name
  name        = "api"
  type        = "A"
  target      = data.terraform_remote_state.lightsail.outputs.lb_dns_name
  is_alias    = true

  lifecycle {
    create_before_destroy = false
  }
}

resource "aws_lightsail_domain_entry" "admin" {
  domain_name = aws_lightsail_domain.staircrusher_club.domain_name
  name        = "admin"
  type        = "A"
  target      = data.terraform_remote_state.lightsail.outputs.lb_dns_name
  is_alias    = true

  lifecycle {
    create_before_destroy = false
  }
}

resource "aws_lightsail_domain_entry" "dev_admin" {
  domain_name = aws_lightsail_domain.dev_staircrusher_club.domain_name
  name        = "admin"
  type        = "A"
  target      = data.terraform_remote_state.lightsail.outputs.lb_dns_name
  is_alias    = true

  lifecycle {
    create_before_destroy = false
  }
}

