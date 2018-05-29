package org.apache.log4j.helpers;

import org.apache.log4j.spi.LoggingEvent;



























public class BoundedFIFO
{
  LoggingEvent[] buf;
  int numElements = 0;
  int first = 0;
  int next = 0;
  

  int maxSize;
  

  public BoundedFIFO(int maxSize)
  {
    if (maxSize < 1) {
      throw new IllegalArgumentException("The maxSize argument (" + maxSize + ") is not a positive integer.");
    }
    
    this.maxSize = maxSize;
    buf = new LoggingEvent[maxSize];
  }
  



  public LoggingEvent get()
  {
    if (numElements == 0) {
      return null;
    }
    LoggingEvent r = buf[first];
    buf[first] = null;
    
    if (++first == maxSize) {
      first = 0;
    }
    numElements -= 1;
    return r;
  }
  




  public void put(LoggingEvent o)
  {
    if (numElements != maxSize) {
      buf[next] = o;
      if (++next == maxSize) {
        next = 0;
      }
      numElements += 1;
    }
  }
  



  public int getMaxSize()
  {
    return maxSize;
  }
  



  public boolean isFull()
  {
    return numElements == maxSize;
  }
  





  public int length()
  {
    return numElements;
  }
  
  int min(int a, int b)
  {
    return a < b ? a : b;
  }
  








  public synchronized void resize(int newSize)
  {
    if (newSize == maxSize) {
      return;
    }
    
    LoggingEvent[] tmp = new LoggingEvent[newSize];
    

    int len1 = maxSize - first;
    

    len1 = min(len1, newSize);
    


    len1 = min(len1, numElements);
    

    System.arraycopy(buf, first, tmp, 0, len1);
    

    int len2 = 0;
    if ((len1 < numElements) && (len1 < newSize)) {
      len2 = numElements - len1;
      len2 = min(len2, newSize - len1);
      System.arraycopy(buf, 0, tmp, len1, len2);
    }
    
    buf = tmp;
    maxSize = newSize;
    first = 0;
    numElements = (len1 + len2);
    next = numElements;
    if (next == maxSize) {
      next = 0;
    }
  }
  




  public boolean wasEmpty()
  {
    return numElements == 1;
  }
  




  public boolean wasFull()
  {
    return numElements + 1 == maxSize;
  }
}
