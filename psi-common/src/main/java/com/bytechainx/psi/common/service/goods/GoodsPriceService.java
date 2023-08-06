package com.bytechainx.psi.common.service.goods;


import java.util.ArrayList;
import java.util.List;

import com.bytechainx.psi.common.EnumConstant.DataStatusEnum;
import com.bytechainx.psi.common.model.GoodsPrice;
import com.bytechainx.psi.common.service.base.CommonService;
import com.jfinal.kit.Kv;
import com.jfinal.kit.Ret;
import com.jfinal.plugin.activerecord.Page;


/**
* 价格查询
*/
public class GoodsPriceService extends CommonService {


	/**
	* 分页列表
	*/
	public Page<GoodsPrice> paginate(Kv goodsConditionColumns, Kv conditionColumns, Integer pageNumber, int pageSize) {
		StringBuffer where = new StringBuffer();
		List<Object> params = new ArrayList<Object>();
		where.append("where 1 = 1 ");
		
		if(goodsConditionColumns != null && !goodsConditionColumns.isEmpty()) {
			StringBuffer goodsWhere = new StringBuffer();
			goodsWhere.append("select id from goods_info where 1 = 1 ");
			
			for (Object key : goodsConditionColumns.keySet()) {
				Object value = goodsConditionColumns.get(key);
				if(value == null) {
					continue;
				}
				if(value instanceof String) {
					goodsWhere.append(" and "+ key +"  like '%"+value+"%'");
				} else if(value instanceof Integer) {
					goodsWhere.append(" and "+ key +"  = "+value);
				}
			}
			where.append(" and goods_info_id in ("+goodsWhere+")");
		}

		conditionFilter(conditionColumns, where, params);
		
		where.append(" and data_status != ?");
		params.add(DataStatusEnum.delete.getValue());

		return GoodsPrice.dao.paginate(pageNumber, pageSize, "select * ", "from goods_price "+where.toString()+" order by id desc", params.toArray());
	}

	/**
	* 修改
	*/
	public Ret update() {

		return Ret.ok();
	}

}