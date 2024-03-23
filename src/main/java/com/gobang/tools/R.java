package com.gobang.tools;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一数据格式
 */
@Data
public class R implements Serializable {
    //状态码
    private Integer code;
    //状态码描述信息
    private String msg;
    //返回的数据
    private Object data;
    /**
     * 操作成功返回的结果
     */
    public static R success(Object data){
        R r=new R();
        r.setCode(200);
        r.setMsg("");
        r.setData(data);
        return r;
    }

    public static R success(int code,Object data){
        R R=new R();
        R.setCode(code);
        R.setMsg("");
        R.setData(data);
        return R;
    }

    public static R success(int code,String msg,Object data){
        R R=new R();
        R.setCode(code);
        R.setMsg(msg);
        R.setData(data);
        return R;
    }

    /**
     * 操作失败返回的结果
     */
    public static R fail(int code,String msg){
        R R=new R();
        R.setCode(code);
        R.setMsg(msg);
        R.setData(null);
        return R;
    }

    public static R fail(int code,String msg,Object data){
        R R=new R();
        R.setCode(code);
        R.setMsg(msg);
        R.setData(data);
        return R;
    }

}
