package org.celllife.mobilisr.servlet;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class ServletContextProviderListener
  implements ServletContextListener
{
  private static volatile ServletContext context;

  static ServletContext getServletContext()
  {
    return context;
  }

  public void contextInitialized(ServletContextEvent event)
  {
    context = event.getServletContext();
  }

  public void contextDestroyed(ServletContextEvent event)
  {
    context = null;
  }
}