package com.jcwhatever.bukkit.storefront.data;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.jcwhatever.bukkit.storefront.data.PaginatedSaleItems.PaginatorPageType;

@Target({ElementType.TYPE}) 
@Retention(RetentionPolicy.RUNTIME)
public @interface IViewInfo {
    
    public PaginatorPageType pageType();

}
