resource "aws_sqs_queue" "dev_common_queue" {
  name = "scc-dev-common-queue"
  sqs_managed_sse_enabled = true
}

resource "aws_sqs_queue" "dev_common_dead_letter_queue" {
  name = "scc-dev-common-dead-letter-queue"
  sqs_managed_sse_enabled = true
  message_retention_seconds = 1209600 # 14 days
}

resource "aws_sqs_queue_redrive_policy" "dev_common_queue_redrive_policy" {
  queue_url = aws_sqs_queue.dev_common_queue.id
  redrive_policy = jsonencode({
    deadLetterTargetArn = aws_sqs_queue.dev_common_dead_letter_queue.arn
    maxReceiveCount = 3
  })
}

resource "aws_sqs_queue_redrive_allow_policy" "dev_common_dead_letter_queue_redrive_allow_policy" {
  queue_url = aws_sqs_queue.dev_common_dead_letter_queue.id
  redrive_allow_policy = jsonencode({
    redrivePermission = "byQueue"
    sourceQueueArns = [aws_sqs_queue.dev_common_queue.arn]
  })
}

resource "aws_sqs_queue" "common_queue" {
  name = "scc-common-queue"
  sqs_managed_sse_enabled = true
}

resource "aws_sqs_queue" "common_dead_letter_queue" {
  name = "scc-common-dead-letter-queue"
  sqs_managed_sse_enabled = true
  message_retention_seconds = 1209600 # 14 days
}

resource "aws_sqs_queue_redrive_policy" "common_queue_redrive_policy" {
  queue_url = aws_sqs_queue.common_queue.id
  redrive_policy = jsonencode({
    deadLetterTargetArn = aws_sqs_queue.common_dead_letter_queue.arn
    maxReceiveCount = 3
  })
}

resource "aws_sqs_queue_redrive_allow_policy" "common_dead_letter_queue_redrive_allow_policy" {
  queue_url = aws_sqs_queue.common_dead_letter_queue.id
  redrive_allow_policy = jsonencode({
    redrivePermission = "byQueue"
    sourceQueueArns = [aws_sqs_queue.common_queue.arn]
  })
}
