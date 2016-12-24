package akkainaction.chapter13

case class ParseError(logId: String, msg: String) extends RuntimeException {
  override def getMessage: String = s"Failed writing to $logId because of $msg"
}
