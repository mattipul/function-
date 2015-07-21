package function;

import java.util.ArrayList;

public class Tape {
    
    private ArrayList<String> data;
    private int pointer;

    public Tape() {
        pointer=0;
        this.data=new ArrayList<String>();
    }
    
    public void left(){
        if(pointer>0){
            pointer--;
        }
    }
    
    public void right(){
        if(pointer<this.data.size()){
            pointer++;
        }
    }
    
    public void set(String v){
        if(pointer==data.size()){
            data.add(v);
        }else if(pointer<data.size()){
            data.set(pointer, v);
        }        
    }
    
    public void remove(int i){
        data.remove(i);
    }
    
    public String get(){
        if(this.data.size()>pointer && pointer>=0){
            return this.data.get(pointer);
        }
        return null;
    }
    
}
