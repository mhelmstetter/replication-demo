package com.mongodb.gui.util;

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.swing.ImageIcon;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

@Component
public class EclipseIcons {

    @Autowired
     ResourceLoader resourceLoader;

    
    public  ImageIcon CLOSE_VIEW = null;

    public  ImageIcon LOCK = null;

    public  ImageIcon FILTER = null;

    public  ImageIcon REMOVE_ALL = null;
    
    public  ImageIcon START = null;
    
    public  ImageIcon TERMINATE = null;
    
    public  ImageIcon WARNING = null;
    
    public  ImageIcon ANTENNA = null;
    
    public  ImageIcon HISTORY = null;
    
    public  ImageIcon PLUS = null;
    
    public  ImageIcon PLUS2 = null;
    
    public  ImageIcon MINUS = null;
    
    public  ImageIcon TABLE_JOIN = null;
    
    public  ImageIcon NEW_PAGE = null;
    
    public  ImageIcon NEW_OBJECT = null;
    
    public  ImageIcon NEW_QUERY = null;
    
    public  ImageIcon WOMAN4 = null;
    
    public  ImageIcon PACKAGE = null;
    
    public  ImageIcon PACKAGE_OUT = null;
    
    public  ImageIcon SAVE = null;
    
    public  ImageIcon FOLDER = null;
    
    public  ImageIcon TAG = null;

  
    @PostConstruct
     public void init() {
        CLOSE_VIEW = loadIcon("close_view.gif");
        LOCK = loadIcon("lock.gif");
        FILTER = loadIcon("filter.gif");
        REMOVE_ALL = loadIcon("removeAll_co.gif");
        START = loadIcon("start.gif");
        TERMINATE = loadIcon("terminate_co.gif");
        WARNING = loadIcon("warning.gif");
        ANTENNA = loadIcon("antenna.gif");
        HISTORY = loadIcon("history.gif");
        PLUS = loadIcon("Plus.gif");
        MINUS = loadIcon("Minus.gif");
        PLUS2 = loadIcon("plus2.gif");
        TABLE_JOIN = loadIcon("table_join.gif");
        NEW_PAGE = loadIcon("new_page.gif");
        NEW_OBJECT = loadIcon("new_object.gif");
        NEW_QUERY = loadIcon("new_query.gif");
        WOMAN4 = loadIcon("woman4.png");
        PACKAGE = loadIcon("package_obj.gif");
        PACKAGE_OUT = loadIcon("package_out.gif");
        SAVE = loadIcon("save_edit.gif");
        FOLDER = loadIcon("fldr_obj.gif");
        TAG = loadIcon("tag.gif");
    }

    private ImageIcon loadIcon(String fileName) {
        try {
            ImageIcon icon = new ImageIcon(resourceLoader.getResource("classpath:/images/" + fileName).getURL());
            return icon;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

}
