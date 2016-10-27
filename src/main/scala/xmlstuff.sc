import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule

val mapper = new ObjectMapper()
mapper.registerModule(DefaultScalaModule)

val a =
  """<hbcGetOrderListServiceReturn>&lt;?xml version="1.0" encoding="UTF-8"?&gt;
    |&lt;OrderList LastOrderHeaderKey="201604192245545879952544"
    |    LastRecordSet="N" ReadFromHistory="B" TotalOrderList="3"&gt;
    |    &lt;Order BillToID="praveen.premchand@partners.hbc.ca"
    |        EnterpriseCode="LT" OrderDate="2016-04-29T12:29:05-04:00"
    |        OrderNo="1217947109" Status="Released"&gt;
    |        &lt;PriceInfo TotalAmount="41.66"/&gt;
    |    &lt;/Order&gt;
    |    &lt;Order BillToID="praveen.premchand@partners.hbc.ca"
    |        EnterpriseCode="LT" OrderDate="2016-04-19T17:04:14-04:00"
    |        OrderNo="1217944107" Status="Released"&gt;
    |        &lt;PriceInfo TotalAmount="180.95"/&gt;
    |    &lt;/Order&gt;
    |    &lt;Order BillToID="praveen.premchand@partners.hbc.ca"
    |        EnterpriseCode="LT" OrderDate="2016-04-19T18:05:33-04:00"
    |        OrderNo="1217946105" Status="Released"&gt;
    |        &lt;PriceInfo TotalAmount="180.95"/&gt;
    |    &lt;/Order&gt;
    |&lt;/OrderList&gt;
    |</hbcGetOrderListServiceReturn>""".stripMargin

import scala.xml.XML._
val b = loadString(a)

b.text
