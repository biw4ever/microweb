package com.yjz.microweb.cache;

/**
 * 缓存访问记录
 * 
 * @ClassName VisitRecord
 * @Description TODO(这里用一句话描述这个类的作用)
 * @author biw
 * @Date 2017年5月31日 上午10:04:59
 * @version 1.0.0
 */
class VisitRecord extends Record
{
    final String shortUri;
    
    final long visitTime;
    
    VisitRecord(String shortUri, long visitTime)
    {
        this.shortUri = shortUri;
        this.visitTime = visitTime;
    }
}