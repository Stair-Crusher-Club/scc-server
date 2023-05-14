resource "aws_lightsail_lb" "scc_lb" {
  name              = "scc_lb"
  health_check_path = "/"
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
