package com.bytechainx.psi.common.service.goods;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.bytechainx.psi.common.model.GoodsInfo;
import com.bytechainx.psi.common.model.GoodsPrice;
import com.bytechainx.psi.common.model.GoodsPrintTag;
import com.bytechainx.psi.common.service.base.CommonService;
import com.jfinal.kit.Kv;
import com.jfinal.kit.Ret;
import com.jfinal.plugin.activerecord.Page;


/**
* 打印标签
*/
public class GoodsPrintTagService extends CommonService {


	/**
	* 分页列表
	 * @param condKv 
	*/
	public Page<GoodsPrintTag> paginate(Kv conditionColumns, Integer pageNumber, int pageSize) {
		StringBuffer where = new StringBuffer();
		List<Object> params = new ArrayList<Object>();
		where.append("where 1 = 1 ");

		conditionFilter(conditionColumns, where, params);
		
		return GoodsPrintTag.dao.paginate(pageNumber, pageSize, "select * ", "from goods_print_tag "+where.toString()+" order by id desc", params.toArray());
	}

	/**
	* 新增
	*/
	public Ret create(GoodsPrintTag printTag) {
		if(printTag == null) {
			return Ret.fail("参数错误");
		}
		if(printTag.getGoodsInfoId() == null) {
			return Ret.fail("商品不能为空");
		}
		if(printTag.getGoodsNumber() == null || printTag.getGoodsNumber() <= 0) {
			return Ret.fail("打印份数不能为空");
		}
		if(printTag.getSpec1Id() == null) {
			printTag.setSpec1Id(0);
		}
		if(printTag.getSpecOption1Id() == null) {
			printTag.setSpecOption1Id(0);
		}
		if(printTag.getSpec2Id() == null) {
			printTag.setSpec2Id(0);
		}
		if(printTag.getSpecOption2Id() == null) {
			printTag.setSpecOption2Id(0);
		}
		if(printTag.getSpec3Id() == null) {
			printTag.setSpec3Id(0);
		}
		if(printTag.getSpecOption3Id() == null) {
			printTag.setSpecOption3Id(0);
		}
		GoodsInfo goodsInfo = printTag.getGoodsInfo();
		GoodsPrice goodsPrice = GoodsPrice.dao.findBySpec(printTag.getGoodsInfoId(), printTag.getSpec1Id(), printTag.getSpecOption1Id(), printTag.getSpec2Id(), printTag.getSpecOption2Id(), printTag.getSpec3Id(), printTag.getSpecOption3Id(), printTag.getUnitId());
		if(goodsPrice != null && StringUtils.isNotEmpty(goodsPrice.getBarCode())) {
			printTag.setBarCode(goodsPrice.getBarCode());
		} else {
			printTag.setBarCode(goodsInfo.getBarCode());
		}
		printTag.setCreatedAt(new Date());
		printTag.setUpdatedAt(new Date());
		printTag.save();
		
		return Ret.ok().set("targetId", printTag.getId());
	}



	/**
	* 删除
	*/
	public Ret delete(List<Integer> ids) {
		if(ids == null || ids.isEmpty()) {
			return Ret.fail("参数错误");
		}
		for (Integer id : ids) {
			GoodsPrintTag.dao.deleteById(id);
		}
		return Ret.ok();
	}
	
}