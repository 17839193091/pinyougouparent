package entity;

import java.io.Serializable;
import java.util.List;

/**
 * 描述:
 *  分页结果实体类
 * @author hudongfei
 * @create 2018-10-20 10:03
 */
public class PageResult implements Serializable {
    private long total;
    private List rows;

    public PageResult(long total, List rows) {
        this.total = total;
        this.rows = rows;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public List getRows() {
        return rows;
    }

    public void setRows(List rows) {
        this.rows = rows;
    }
}
