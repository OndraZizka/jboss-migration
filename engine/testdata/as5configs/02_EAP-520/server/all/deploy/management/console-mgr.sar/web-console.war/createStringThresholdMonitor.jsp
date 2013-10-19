<%@ page import="javax.management.MBeanServer,
                 org.jboss.mx.util.MBeanServerLocator,
                 org.jboss.mx.util.InstanceOfQueryExp,
                 java.util.Set,
                 java.util.Iterator,
                 javax.management.ObjectInstance,
                 javax.management.ObjectName,
                 java.util.HashSet"%>
 <%--
 |
 |  Author: Bill Burke    (bill@jboss.org)
 |
 | Distributable under LGPL license.
 | See terms of license at gnu.org.
 +--%>
<%!
 
   /**
    * Translate HTML tags and single and double quotes.
    */
   public String translateMetaCharacters(Object value)
   {
      if(value == null) 
         return null;
          
      String s = String.valueOf(value);   
      String sanitizedName = s.replace("<", "&lt;");
      sanitizedName = sanitizedName.replace(">", "&gt;");
      sanitizedName = sanitizedName.replace("\"", "&quot;");
      sanitizedName = sanitizedName.replace("\'", "&apos;");
      return sanitizedName;
   }
%>
 
<%
   MBeanServer mbeanServer = MBeanServerLocator.locateJBoss();
   String error = (String)request.getAttribute("error");
   String monitorName = request.getParameter("monitorName");
   if (monitorName == null) monitorName = "";
   String objectName = request.getParameter("objectName");
   if (objectName == null) objectName = "";
   String attribute = request.getParameter("attribute");
   if (attribute == null) attribute = "";
   String threshold = request.getParameter("threshold");
   if (threshold == null && objectName != null && attribute != null)
   {
      threshold = (String)mbeanServer.getAttribute(new ObjectName(objectName), attribute);
   }
   else if (threshold == null) threshold = "";

   String period = request.getParameter("period");
   if (period == null) period = "";
   String equality = request.getParameter("equality");
   if (equality == null) equality = "";
   else equality = "checked";
   String enabled = request.getParameter("enabled");
   if (enabled == null) enabled = "";
   else enabled = "checked";
   String[] alertStrings = request.getParameterValues("alerts");
   HashSet alertSet = null;
   if (alertStrings != null)
   {
      alertSet = new HashSet();
      for (int i = 0; i < alertStrings.length; i++)
      {
         alertSet.add(alertStrings[i]);
      }
   }



%>
<?xml version="1.0" encoding="iso-8859-1"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>JBoss Management Console - Create String Threshold Monitor</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
<link rel="StyleSheet" href="css/jboss.css" type="text/css"/>
</head>
<body>
<!-- header begin -->
	<img src="images/logo.gif" alt="JBoss" id="logo" width="226" height="105" />
	<div id="header">
		&nbsp;</div>
	<div id="navigation_bar">
	</div>
<!-- header end -->
<hr class="hide"/>
	<center>
	<div id="content">
		<div class="content_block" style="width: 100%; height: 247">
			<h3>Create String Threshold MBean Monitor</h3>
					<p>&nbsp;</p>
<%
   if (error != null)
   {
%>
					<p><font color="red" size ="-2"><%=error%></font> </p>
<%
   }
%>
<form action="CreateStringThresholdMonitor" method="post">
<table cellspacing="2" cellpadding="2" border="0">
<tr>
    <td><b>Monitor Name</b></td>
    <td><input type="text" name="monitorName" size="35" value="<%=translateMetaCharacters(monitorName)%>"></td>
    <td><i>The name of the monitor and how it will be references within web console</i></td>
</tr>
<tr>
    <td><b>Object Name</b></td>
    <td><input type="text" name="objectName" value="<%=translateMetaCharacters(objectName)%>" size="35"></td>
    <td><i>The MBean javax.management.ObjectName of the MBean you are monitoring</i></td>
</tr>
<tr>
    <td><b>Attribute</b></td>
    <td><input type="text" name="attribute" value="<%=translateMetaCharacters(attribute)%>"  size="35"></td>
    <td><i>The MBean Attribute you are monitoring</i></td>
</tr>
<tr>
    <td><b>Threshold</b></td>
    <td><input type="text" name="threshold" size="35" value="<%=translateMetaCharacters(threshold)%>"></td>
    <td><i>The value that will trigger an alert when the Comparison Equation is reached for the attribute value</i></td>
</tr>
<tr>
    <td><b>Time Period</b></td>
    <td><input type="text" name="period" size="35" value="<%=translateMetaCharacters(period)%>"></td>
    <td><i>How often should threshold be tested.</i></td>
</tr>
<tr>
    <td><b>Equality Trigger</b></td>
    <td><input type="checkbox" name="equality" value="" <%=equality%>></td>
    <td><i>Uncheck this box if you want an alert to trigger when attribute changes from threshold value</i></td>
</tr>
<tr>
    <td><b>Persisted</b></td>
    <td><input type="checkbox" name="persisted" value="" checked></td>
    <td><i>Should this monitor be created for next JBoss reboot?</i></td>
</tr>
<tr>
    <td><b>Enable Monitor</b></td>
    <td><input type="checkbox" name="enabled" value="" <%=enabled%>></td>
    <td><i>Should this monitor be enabled.</i></td>
</tr>
<tr>
    <td><b>Alerts</b></td>
    <td>
<%
   InstanceOfQueryExp queryExp = null;
   queryExp = new InstanceOfQueryExp("org.jboss.monitor.alerts.JBossAlertListener");
   Set alerts = mbeanServer.queryNames(null, queryExp);
   if (alerts.size() > 0)
   {
%>
   <select name="alerts" id="alerts" size="<%=Integer.toString(alerts.size())%>" multiple>
<%
      Iterator it = alerts.iterator();
      while (it.hasNext())
      {
         ObjectName alert = (ObjectName)it.next();
         String alertName = (String)mbeanServer.getAttribute(alert, "AlertName");
         String selected = "";
         if (alertSet != null && alertSet.contains(alert.toString())) selected = "SELECTED";
%>
   <option value="<%=alert.toString()%>" <%=selected%>><%=alertName%></option>
<%
      }
   }
%>
</select>

    </td>
    <td><i>Alert Listeners to trigger.</i></td>
</tr>
</table>
<input type="submit" value="Create">
</form>
		</div>
		<div class="spacer"><hr/></div>
	</div>
	</center>
<!-- content end -->

<hr class="hide"/>
<!-- footer begin -->
	<div id="footer">
		<div id="credits">JBoss&trade; Management Console</div>
		<div id="footer_bar">&nbsp;</div>
	</div>
<!-- footer end -->
</body>
</html>
