package org.apache.log4j.jmx;

import java.lang.reflect.Constructor;
import java.util.Vector;
import javax.management.Attribute;
import javax.management.AttributeNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.JMException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.MBeanServer;
import javax.management.Notification;
import javax.management.NotificationBroadcaster;
import javax.management.NotificationBroadcasterSupport;
import javax.management.NotificationFilter;
import javax.management.NotificationFilterSupport;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.RuntimeOperationsException;
import org.apache.log4j.Appender;
import org.apache.log4j.Category;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.helpers.OptionConverter;
import org.apache.log4j.spi.HierarchyEventListener;
import org.apache.log4j.spi.LoggerRepository;




















public class HierarchyDynamicMBean
  extends AbstractDynamicMBean
  implements HierarchyEventListener, NotificationBroadcaster
{
  static final String ADD_APPENDER = "addAppender.";
  static final String THRESHOLD = "threshold";
  private MBeanConstructorInfo[] dConstructors = new MBeanConstructorInfo[1];
  private MBeanOperationInfo[] dOperations = new MBeanOperationInfo[1];
  
  private Vector vAttributes = new Vector();
  private String dClassName = getClass().getName();
  private String dDescription = "This MBean acts as a management facade for org.apache.log4j.Hierarchy.";
  

  private NotificationBroadcasterSupport nbs = new NotificationBroadcasterSupport();
  

  private LoggerRepository hierarchy;
  
  private static Logger log = Logger.getLogger(HierarchyDynamicMBean.class);
  
  public HierarchyDynamicMBean() {
    hierarchy = LogManager.getLoggerRepository();
    buildDynamicMBeanInfo();
  }
  
  private void buildDynamicMBeanInfo()
  {
    Constructor[] constructors = getClass().getConstructors();
    dConstructors[0] = new MBeanConstructorInfo("HierarchyDynamicMBean(): Constructs a HierarchyDynamicMBean instance", constructors[0]);
    


    vAttributes.add(new MBeanAttributeInfo("threshold", "java.lang.String", "The \"threshold\" state of the hiearchy.", true, true, false));
    





    MBeanParameterInfo[] params = new MBeanParameterInfo[1];
    params[0] = new MBeanParameterInfo("name", "java.lang.String", "Create a logger MBean");
    
    dOperations[0] = new MBeanOperationInfo("addLoggerMBean", "addLoggerMBean(): add a loggerMBean", params, "javax.management.ObjectName", 1);
  }
  





  public ObjectName addLoggerMBean(String name)
  {
    Logger cat = LogManager.exists(name);
    
    if (cat != null) {
      return addLoggerMBean(cat);
    }
    return null;
  }
  
  ObjectName addLoggerMBean(Logger logger)
  {
    String name = logger.getName();
    ObjectName objectName = null;
    try {
      LoggerDynamicMBean loggerMBean = new LoggerDynamicMBean(logger);
      objectName = new ObjectName("log4j", "logger", name);
      
      if (!server.isRegistered(objectName)) {
        registerMBean(loggerMBean, objectName);
        NotificationFilterSupport nfs = new NotificationFilterSupport();
        nfs.enableType("addAppender." + logger.getName());
        log.debug("---Adding logger [" + name + "] as listener.");
        nbs.addNotificationListener(loggerMBean, nfs, null);
        vAttributes.add(new MBeanAttributeInfo("logger=" + name, "javax.management.ObjectName", "The " + name + " logger.", true, true, false));
      }
      

    }
    catch (JMException e)
    {

      log.error("Could not add loggerMBean for [" + name + "].", e);
    } catch (RuntimeException e) {
      log.error("Could not add loggerMBean for [" + name + "].", e);
    }
    return objectName;
  }
  


  public void addNotificationListener(NotificationListener listener, NotificationFilter filter, Object handback)
  {
    nbs.addNotificationListener(listener, filter, handback);
  }
  
  protected Logger getLogger()
  {
    return log;
  }
  


  public MBeanInfo getMBeanInfo()
  {
    MBeanAttributeInfo[] attribs = new MBeanAttributeInfo[vAttributes.size()];
    vAttributes.toArray(attribs);
    
    return new MBeanInfo(dClassName, dDescription, attribs, dConstructors, dOperations, new MBeanNotificationInfo[0]);
  }
  





  public MBeanNotificationInfo[] getNotificationInfo()
  {
    return nbs.getNotificationInfo();
  }
  



  public Object invoke(String operationName, Object[] params, String[] signature)
    throws MBeanException, ReflectionException
  {
    if (operationName == null) {
      throw new RuntimeOperationsException(new IllegalArgumentException("Operation name cannot be null"), "Cannot invoke a null operation in " + dClassName);
    }
    



    if (operationName.equals("addLoggerMBean")) {
      return addLoggerMBean((String)params[0]);
    }
    throw new ReflectionException(new NoSuchMethodException(operationName), "Cannot find the operation " + operationName + " in " + dClassName);
  }
  








  public Object getAttribute(String attributeName)
    throws AttributeNotFoundException, MBeanException, ReflectionException
  {
    if (attributeName == null) {
      throw new RuntimeOperationsException(new IllegalArgumentException("Attribute name cannot be null"), "Cannot invoke a getter of " + dClassName + " with null attribute name");
    }
    


    log.debug("Called getAttribute with [" + attributeName + "].");
    

    if (attributeName.equals("threshold"))
      return hierarchy.getThreshold();
    if (attributeName.startsWith("logger")) {
      int k = attributeName.indexOf("%3D");
      String val = attributeName;
      if (k > 0) {
        val = attributeName.substring(0, k) + '=' + attributeName.substring(k + 3);
      }
      try {
        return new ObjectName("log4j:" + val);
      } catch (JMException e) {
        log.error("Could not create ObjectName" + val);
      } catch (RuntimeException e) {
        log.error("Could not create ObjectName" + val);
      }
    }
    



    throw new AttributeNotFoundException("Cannot find " + attributeName + " attribute in " + dClassName);
  }
  



  public void addAppenderEvent(Category logger, Appender appender)
  {
    log.debug("addAppenderEvent called: logger=" + logger.getName() + ", appender=" + appender.getName());
    
    Notification n = new Notification("addAppender." + logger.getName(), this, 0L);
    n.setUserData(appender);
    log.debug("sending notification.");
    nbs.sendNotification(n);
  }
  
  public void removeAppenderEvent(Category cat, Appender appender)
  {
    log.debug("removeAppenderCalled: logger=" + cat.getName() + ", appender=" + appender.getName());
  }
  

  public void postRegister(Boolean registrationDone)
  {
    log.debug("postRegister is called.");
    hierarchy.addHierarchyEventListener(this);
    Logger root = hierarchy.getRootLogger();
    addLoggerMBean(root);
  }
  
  public void removeNotificationListener(NotificationListener listener)
    throws ListenerNotFoundException
  {
    nbs.removeNotificationListener(listener);
  }
  




  public void setAttribute(Attribute attribute)
    throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException
  {
    if (attribute == null) {
      throw new RuntimeOperationsException(new IllegalArgumentException("Attribute cannot be null"), "Cannot invoke a setter of " + dClassName + " with null attribute");
    }
    

    String name = attribute.getName();
    Object value = attribute.getValue();
    
    if (name == null) {
      throw new RuntimeOperationsException(new IllegalArgumentException("Attribute name cannot be null"), "Cannot invoke the setter of " + dClassName + " with null attribute name");
    }
    



    if (name.equals("threshold")) {
      Level l = OptionConverter.toLevel((String)value, hierarchy.getThreshold());
      
      hierarchy.setThreshold(l);
    }
  }
}