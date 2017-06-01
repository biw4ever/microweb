package com.yjz.microweb.cache;

/**
 * 缓存保存记录
 * 
 * @ClassName SaveRecord
 * @Description TODO(这里用一句话描述这个类的作用)
 * @author biw
 * @Date 2017年5月31日 上午10:04:48
 * @version 1.0.0
 */
class SaveRecord extends Record
{
    
    final SaveTimeRecord saveTimeRecord;
    
    final byte[] value;
    
    SaveRecord(String shortUri, long saveTime, byte[] value, int length)
    {
        saveTimeRecord = new SaveTimeRecord(shortUri, saveTime, length);
        this.value = value;
    }
}