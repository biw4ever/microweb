package com.yjz.microweb.cache;

/**
 * 静态资源缓存 实现者应该注意过期策略，避免内存被大量占用
 * 
 * @ClassName ResourceCache
 * @Description TODO(这里用一句话描述这个类的作用)
 * @author biw
 * @Date 2017年5月31日 上午10:04:26
 * @version 1.0.0
 */
public interface ResourceCache
{
    
    public boolean containsKey(String shortUri);
    
    /**
     * 1.返回缓存数据（如果未缓存，返回空；如果缓存的是null对象，返回new byte[0]；其它返回实际缓存的对象）<br>
     * 2.记录访问事件(队列满或者清理线程执行时停止接受任务)<br>
     */
    public byte[] getCache(String shortUri);
    
    /**
     * 海量无效请求可能导致大量的磁盘IO，因此无效资源地址也应该进行缓存； 但为避免大量的无效key产生，应注意实现过期策略。
     * 
     * @param shortUri
     * @param bytes
     */
    public void putCache(String shortUri, byte[] bytes);
    
}