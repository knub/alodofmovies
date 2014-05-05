package lod2014group1.web

import org.scalatra._
import scalate.ScalateSupport

class MovieSearchServlet extends MovieSearchStack {

  get("/") {
    <html>
      <body>
        <h1>Hello, world!</h1>
        Say <a href="hello-scalate">hello to Scalate</a>.
      </body>
    </html>
  }
  
}
