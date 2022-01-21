package wekimini.util;

import com.thoughtworks.xstream.XStream;

public class WekStream extends XStream {
  public WekStream() {
    allowTypesByWildcard(new String[] { "wekimini.**" });
  }
}
