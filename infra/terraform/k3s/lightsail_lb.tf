resource "aws_lightsail_lb" "scc_lb" {
  name              = "scc_lb"
  health_check_path = "/healthz"
  instance_port     = var.node_port
}

resource "aws_lightsail_lb_attachment" "k3s" {
  for_each = toset(
    concat(
      [aws_lightsail_instance.k3s_control_plane.name],
      [for k, v in aws_lightsail_instance.k3s_data_planes : v.name],
    )
  )
  lb_name       = aws_lightsail_lb.scc_lb.name
  instance_name = each.key
}

# it does not work correctly...
# resource "aws_lightsail_lb_certificate" "scc_lb_cert" {
#   name        = "scc-load-balancer-certificate"
#   lb_name     = aws_lightsail_lb.scc_lb.id
#   domain_name = "staircrusher.club"
# }

# resource "aws_lightsail_lb_certificate_attachment" "scc_lb_cert_attachment" {
#   lb_name          = aws_lightsail_lb.scc_lb.name
#   certificate_name = aws_lightsail_lb_certificate.scc_lb_cert.name
# }

# resource "aws_lightsail_lb_https_redirection_policy" "scc_lb_https" {
#   lb_name = aws_lightsail_lb.scc_lb.name
#   enabled = true
# }
