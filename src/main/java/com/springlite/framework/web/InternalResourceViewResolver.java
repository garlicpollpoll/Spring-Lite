package com.springlite.framework.web;

public class InternalResourceViewResolver implements ViewResolver {
    
    private String prefix = "";
    private String suffix = "";
    
    public InternalResourceViewResolver() {
    }
    
    public InternalResourceViewResolver(String prefix, String suffix) {
        this.prefix = prefix != null ? prefix : "";
        this.suffix = suffix != null ? suffix : "";
    }
    
    @Override
    public View resolveViewName(String viewName) throws Exception {
        if (viewName == null) {
            return null;
        }
        
        String url = prefix + viewName + suffix;
        return new JspView(url);
    }
    
    public String getPrefix() {
        return prefix;
    }
    
    public void setPrefix(String prefix) {
        this.prefix = prefix != null ? prefix : "";
    }
    
    public String getSuffix() {
        return suffix;
    }
    
    public void setSuffix(String suffix) {
        this.suffix = suffix != null ? suffix : "";
    }
    
    @Override
    public String toString() {
        return "InternalResourceViewResolver{" +
                "prefix='" + prefix + '\'' +
                ", suffix='" + suffix + '\'' +
                '}';
    }
} 