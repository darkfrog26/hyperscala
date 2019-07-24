package specs

import io.youi.net.ContentType
import org.scalatest.Matchers
import org.scalatest.wordspec.AnyWordSpec

class ContentTypeSpec extends AnyWordSpec with Matchers {
  "ContentType" should {
    "parse a massive type" in {
      val s = """multipart/related;start="<rootpart*1faa50c8-1aec-4659-ba8b-372a789b1945@example.jaxws.sun.com>";type="application/xop+xml";boundary="uuid:1faa50c8-1aec-4659-ba8b-372a789b1945";start-info="text/xml""""
      val ct = ContentType.parse(s)
      ct.`type` should be("multipart")
      ct.subType should be("related")
      ct.outputString should be("""multipart/related; start="<rootpart*1faa50c8-1aec-4659-ba8b-372a789b1945@example.jaxws.sun.com>"; type="application/xop+xml"; boundary="uuid:1faa50c8-1aec-4659-ba8b-372a789b1945"; start-info="text/xml"""")
    }
  }
}