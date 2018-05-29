package org.apache.log4j.helpers;

import org.apache.log4j.spi.LoggingEvent;



































public class CyclicBuffer
{
  LoggingEvent[] ea;
  int first;
  int last;
  int numElems;
  int maxSize;
  
  public CyclicBuffer(int maxSize)
    throws IllegalArgumentException
  {
    if (maxSize < 1) {
      throw new IllegalArgumentException("The maxSize argument (" + maxSize + ") is not a positive integer.");
    }
    
    this.maxSize = maxSize;
    ea = new LoggingEvent[maxSize];
    first = 0;
    last = 0;
    numElems = 0;
  }
  




  public void add(LoggingEvent event)
  {
    ea[last] = event;
    if (++last == maxSize) {
      last = 0;
    }
    if (numElems < maxSize) {
      numElems += 1;
    } else if (++first == maxSize) {
      first = 0;
    }
  }
  







  public LoggingEvent get(int i)
  {
    if ((i < 0) || (i >= numElems)) {
      return null;
    }
    return ea[((first + i) % maxSize)];
  }
  
  public int getMaxSize()
  {
    return maxSize;
  }
  




  public LoggingEvent get()
  {
    LoggingEvent r = null;
    if (numElems > 0) {
      numElems -= 1;
      r = ea[first];
      ea[first] = null;
      if (++first == maxSize)
        first = 0;
    }
    return r;
  }
  





  public int length()
  {
    return numElems;
  }
  





  public void resize(int newSize)
  {
    if (newSize < 0) {
      throw new IllegalArgumentException("Negative array size [" + newSize + "] not allowed.");
    }
    
    if (newSize == numElems) {
      return;
    }
    LoggingEvent[] temp = new LoggingEvent[newSize];
    
    int loopLen = newSize < numElems ? newSize : numElems;
    
    for (int i = 0; i < loopLen; i++) {
      temp[i] = ea[first];
      ea[first] = null;
      if (++first == numElems)
        first = 0;
    }
    ea = temp;
    first = 0;
    numElems = loopLen;
    maxSize = newSize;
    if (loopLen == newSize) {
      last = 0;
    } else {
      last = loopLen;
    }
  }
}
