
package com.github.sdnwiselab.sdnwise.cooja;

import com.github.sdnwiselab.sdnwise.packet.*;

interface AbstractApplication {

  public abstract void dataMote(DataPacket packet, Mote mote);

}
