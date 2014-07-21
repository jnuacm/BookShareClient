package group.acm.bookshare.function;

import java.util.Map;

public interface Update {
	public void before();
	public void process(int value);
	public void after(Map<String,Object> map);
}
