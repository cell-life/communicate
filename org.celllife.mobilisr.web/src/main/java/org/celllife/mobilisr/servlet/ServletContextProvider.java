package org.celllife.mobilisr.servlet;

import javax.servlet.ServletContext;

public class ServletContextProvider
{
  public static ServletContext getServletContext()
  {
    return ServletContextProviderListener.getServletContext();
  }
}