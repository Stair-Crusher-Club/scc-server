resource "aws_lightsail_lb" "scc_lb" {
  name              = "scc-lb-1"
  health_check_path = "/"
  instance_port     = "80"
}

resource "aws_lightsail_lb_attachment" "k3s" {
  for_each = toset([
    aws_lightsail_instance.k3s_control_plane.name, 
    aws_lightsail_instance.k3s_data_plane.name,
  ])
  lb_name       = aws_lightsail_lb.scc_lb.name
  instance_name = each.key
}
