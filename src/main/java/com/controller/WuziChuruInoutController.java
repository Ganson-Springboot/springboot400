
package com.controller;

import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.text.SimpleDateFormat;
import com.alibaba.fastjson.JSONObject;
import java.util.*;
import org.springframework.beans.BeanUtils;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.context.ContextLoader;
import javax.servlet.ServletContext;
import com.service.TokenService;
import com.utils.*;
import java.lang.reflect.InvocationTargetException;

import com.service.DictionaryService;
import org.apache.commons.lang3.StringUtils;
import com.annotation.IgnoreAuth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.entity.*;
import com.entity.view.*;
import com.service.*;
import com.utils.PageUtils;
import com.utils.R;
import com.alibaba.fastjson.*;

/**
 * 出入库
 * 后端接口
 * @author
 * @email
*/
@RestController
@Controller
@RequestMapping("/wuziChuruInout")
public class WuziChuruInoutController {
    private static final Logger logger = LoggerFactory.getLogger(WuziChuruInoutController.class);

    private static final String TABLE_NAME = "wuziChuruInout";

    @Autowired
    private WuziChuruInoutService wuziChuruInoutService;


    @Autowired
    private TokenService tokenService;

    @Autowired
    private DictionaryService dictionaryService;//字典
    @Autowired
    private ForumService forumService;//论坛
    @Autowired
    private GonggaoService gonggaoService;//公告
    @Autowired
    private GongyingshangService gongyingshangService;//供应商信息
    @Autowired
    private WuziService wuziService;//物资
    @Autowired
    private WuziChuruInoutListService wuziChuruInoutListService;//出入库详情
    @Autowired
    private WuziOrderService wuziOrderService;//物资分配
    @Autowired
    private WuzikucunService wuzikucunService;//物资库存
    @Autowired
    private YonghuService yonghuService;//用户
    @Autowired
    private UsersService usersService;//管理员


    /**
    * 后端列表
    */
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params, HttpServletRequest request){
        logger.debug("page方法:,,Controller:{},,params:{}",this.getClass().getName(),JSONObject.toJSONString(params));
        String role = String.valueOf(request.getSession().getAttribute("role"));
        if(false)
            return R.error(511,"永不会进入");
        else if("用户".equals(role))
            params.put("yonghuId",request.getSession().getAttribute("userId"));
        CommonUtil.checkMap(params);
        PageUtils page = wuziChuruInoutService.queryPage(params);

        //字典表数据转换
        List<WuziChuruInoutView> list =(List<WuziChuruInoutView>)page.getList();
        for(WuziChuruInoutView c:list){
            //修改对应字典表字段
            dictionaryService.dictionaryConvert(c, request);
        }
        return R.ok().put("data", page);
    }

    /**
    * 后端详情
    */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id, HttpServletRequest request){
        logger.debug("info方法:,,Controller:{},,id:{}",this.getClass().getName(),id);
        WuziChuruInoutEntity wuziChuruInout = wuziChuruInoutService.selectById(id);
        if(wuziChuruInout !=null){
            //entity转view
            WuziChuruInoutView view = new WuziChuruInoutView();
            BeanUtils.copyProperties( wuziChuruInout , view );//把实体数据重构到view中
            //修改对应字典表字段
            dictionaryService.dictionaryConvert(view, request);
            return R.ok().put("data", view);
        }else {
            return R.error(511,"查不到数据");
        }

    }

    /**
    * 后端保存
    */
    @RequestMapping("/save")
    public R save(@RequestBody WuziChuruInoutEntity wuziChuruInout, HttpServletRequest request){
        logger.debug("save方法:,,Controller:{},,wuziChuruInout:{}",this.getClass().getName(),wuziChuruInout.toString());

        String role = String.valueOf(request.getSession().getAttribute("role"));
        if(false)
            return R.error(511,"永远不会进入");

        Wrapper<WuziChuruInoutEntity> queryWrapper = new EntityWrapper<WuziChuruInoutEntity>()
            .eq("wuzi_churu_inout_types", wuziChuruInout.getWuziChuruInoutTypes())
            ;

        logger.info("sql语句:"+queryWrapper.getSqlSegment());
        WuziChuruInoutEntity wuziChuruInoutEntity = wuziChuruInoutService.selectOne(queryWrapper);
        if(wuziChuruInoutEntity==null){
            wuziChuruInout.setInsertTime(new Date());
            wuziChuruInout.setCreateTime(new Date());
            wuziChuruInoutService.insert(wuziChuruInout);
            return R.ok();
        }else {
            return R.error(511,"表中有相同数据");
        }
    }

    /**
    * 后端修改
    */
    @RequestMapping("/update")
    public R update(@RequestBody WuziChuruInoutEntity wuziChuruInout, HttpServletRequest request) throws NoSuchFieldException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        logger.debug("update方法:,,Controller:{},,wuziChuruInout:{}",this.getClass().getName(),wuziChuruInout.toString());
        WuziChuruInoutEntity oldWuziChuruInoutEntity = wuziChuruInoutService.selectById(wuziChuruInout.getId());//查询原先数据

        String role = String.valueOf(request.getSession().getAttribute("role"));
//        if(false)
//            return R.error(511,"永远不会进入");

            wuziChuruInoutService.updateById(wuziChuruInout);//根据id更新
            return R.ok();
    }


    /**
    * 出库
    */
    @RequestMapping("/outWuziChuruInoutList")
    public R outWuziChuruInoutList(@RequestBody  Map<String, Object> params,HttpServletRequest request){
        logger.debug("outWuziChuruInoutList方法:,,Controller:{},,params:{}",this.getClass().getName(),JSONObject.toJSONString(params));

        String role = String.valueOf(request.getSession().getAttribute("role"));



        Map<String, Integer> map = (Map<String, Integer>) params.get("map");
        if(map == null || map.size() == 0)
            return R.error(511,"列表内容不能为空");


        Set<String> ids = map.keySet();

        List<WuziEntity> wuziList = wuziService.selectBatchIds(ids);
        if(wuziList == null || wuziList.size() == 0){
            return R.error(511,"查数据库查不到数据");
        }else{
            for(WuziEntity w:wuziList){
                Integer value = w.getWuziKucunNumber()-map.get(String.valueOf(w.getId()));
                if(value <0){
                    return R.error(511,"出库数量大于库存数量");
                }
                w.setWuziKucunNumber(value);
            }
        }

        //当前表
        WuziChuruInoutEntity wuziChuruInoutEntity = new WuziChuruInoutEntity<>();
            wuziChuruInoutEntity.setWuziChuruInoutUuidNumber(String.valueOf(new Date().getTime()));
            wuziChuruInoutEntity.setWuziChuruInoutTypes(1);
            wuziChuruInoutEntity.setWuziChuruInoutContent("");
            wuziChuruInoutEntity.setInsertTime(new Date());
            wuziChuruInoutEntity.setCreateTime(new Date());

        boolean insertWuziChuruInout = wuziChuruInoutService.insert(wuziChuruInoutEntity);
        //list表
        ArrayList<WuziChuruInoutListEntity> wuziChuruInoutLists = new ArrayList<>();
        if(insertWuziChuruInout){
            for(String id:ids){
                WuziChuruInoutListEntity wuziChuruInoutListEntity = new WuziChuruInoutListEntity();
                    wuziChuruInoutListEntity.setWuziChuruInoutId(wuziChuruInoutEntity.getId());
                    wuziChuruInoutListEntity.setWuziId(Integer.valueOf(id));
                    wuziChuruInoutListEntity.setWuziChuruInoutListNumber(map.get(id));
                    wuziChuruInoutListEntity.setInsertTime(new Date());
                    wuziChuruInoutListEntity.setCreateTime(new Date());
                wuziChuruInoutLists.add(wuziChuruInoutListEntity);
                wuziService.updateBatchById(wuziList);
            }
            wuziChuruInoutListService.insertBatch(wuziChuruInoutLists);
        }

        return R.ok();
    }

    /**
    *入库
    */
    @RequestMapping("/inWuziChuruInoutList")
    public R inWuziChuruInoutList(@RequestBody  Map<String, Object> params,HttpServletRequest request){
        logger.debug("inWuziChuruInoutList方法:,,Controller:{},,params:{}",this.getClass().getName(),JSONObject.toJSONString(params));
        //params:{"map":{"1":2,"2":3},"wuziOutinName":"订单1"}

        String role = String.valueOf(request.getSession().getAttribute("role"));



        Map<String, Integer> map = (Map<String, Integer>) params.get("map");
        if(map == null || map.size() == 0)
            return R.error(511,"列表内容不能为空");

        Set<String> ids = map.keySet();

        List<WuziEntity> wuziList = wuziService.selectBatchIds(ids);
        if(wuziList == null || wuziList.size() == 0){
            return R.error(511,"查数据库查不到数据");
        }else{
            for(WuziEntity w:wuziList){
                w.setWuziKucunNumber(w.getWuziKucunNumber()+map.get(String.valueOf(w.getId())));
            }
        }

        //当前表
        WuziChuruInoutEntity wuziChuruInoutEntity = new WuziChuruInoutEntity<>();
            wuziChuruInoutEntity.setWuziChuruInoutUuidNumber(String.valueOf(new Date().getTime()));
            wuziChuruInoutEntity.setWuziChuruInoutTypes(2);
            wuziChuruInoutEntity.setWuziChuruInoutContent("");
            wuziChuruInoutEntity.setInsertTime(new Date());
            wuziChuruInoutEntity.setCreateTime(new Date());


        boolean insertWuziChuruInout = wuziChuruInoutService.insert(wuziChuruInoutEntity);
        //list表
        ArrayList<WuziChuruInoutListEntity> wuziChuruInoutLists = new ArrayList<>();
        if(insertWuziChuruInout){
            for(String id:ids){
                WuziChuruInoutListEntity wuziChuruInoutListEntity = new WuziChuruInoutListEntity();
                wuziChuruInoutListEntity.setWuziChuruInoutId(wuziChuruInoutEntity.getId());
                wuziChuruInoutListEntity.setWuziId(Integer.valueOf(id));
                wuziChuruInoutListEntity.setWuziChuruInoutListNumber(map.get(id));
                wuziChuruInoutListEntity.setInsertTime(new Date());
                wuziChuruInoutListEntity.setCreateTime(new Date());
                wuziChuruInoutLists.add(wuziChuruInoutListEntity);
                wuziService.updateBatchById(wuziList);
            }
            wuziChuruInoutListService.insertBatch(wuziChuruInoutLists);
        }

        return R.ok();
    }
    /**
    * 删除
    */
    @RequestMapping("/delete")
    public R delete(@RequestBody Integer[] ids, HttpServletRequest request){
        logger.debug("delete:,,Controller:{},,ids:{}",this.getClass().getName(),ids.toString());
        List<WuziChuruInoutEntity> oldWuziChuruInoutList =wuziChuruInoutService.selectBatchIds(Arrays.asList(ids));//要删除的数据
        wuziChuruInoutService.deleteBatchIds(Arrays.asList(ids));
        wuziChuruInoutListService.delete(new EntityWrapper<WuziChuruInoutListEntity>().in("wuzi_churu_inout_id",ids));

        return R.ok();
    }


    /**
     * 批量上传
     */
    @RequestMapping("/batchInsert")
    public R save( String fileName, HttpServletRequest request){
        logger.debug("batchInsert方法:,,Controller:{},,fileName:{}",this.getClass().getName(),fileName);
        Integer yonghuId = Integer.valueOf(String.valueOf(request.getSession().getAttribute("userId")));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            List<WuziChuruInoutEntity> wuziChuruInoutList = new ArrayList<>();//上传的东西
            Map<String, List<String>> seachFields= new HashMap<>();//要查询的字段
            Date date = new Date();
            int lastIndexOf = fileName.lastIndexOf(".");
            if(lastIndexOf == -1){
                return R.error(511,"该文件没有后缀");
            }else{
                String suffix = fileName.substring(lastIndexOf);
                if(!".xls".equals(suffix)){
                    return R.error(511,"只支持后缀为xls的excel文件");
                }else{
                    URL resource = this.getClass().getClassLoader().getResource("static/upload/" + fileName);//获取文件路径
                    File file = new File(resource.getFile());
                    if(!file.exists()){
                        return R.error(511,"找不到上传文件，请联系管理员");
                    }else{
                        List<List<String>> dataList = PoiUtil.poiImport(file.getPath());//读取xls文件
                        dataList.remove(0);//删除第一行，因为第一行是提示
                        for(List<String> data:dataList){
                            //循环
                            WuziChuruInoutEntity wuziChuruInoutEntity = new WuziChuruInoutEntity();
//                            wuziChuruInoutEntity.setWuziChuruInoutUuidNumber(data.get(0));                    //出入库流水号 要改的
//                            wuziChuruInoutEntity.setWuziChuruInoutTypes(Integer.valueOf(data.get(0)));   //出入库类型 要改的
//                            wuziChuruInoutEntity.setWuziChuruInoutContent("");//详情和图片
//                            wuziChuruInoutEntity.setInsertTime(date);//时间
//                            wuziChuruInoutEntity.setCreateTime(date);//时间
                            wuziChuruInoutList.add(wuziChuruInoutEntity);


                            //把要查询是否重复的字段放入map中
                                //出入库流水号
                                if(seachFields.containsKey("wuziChuruInoutUuidNumber")){
                                    List<String> wuziChuruInoutUuidNumber = seachFields.get("wuziChuruInoutUuidNumber");
                                    wuziChuruInoutUuidNumber.add(data.get(0));//要改的
                                }else{
                                    List<String> wuziChuruInoutUuidNumber = new ArrayList<>();
                                    wuziChuruInoutUuidNumber.add(data.get(0));//要改的
                                    seachFields.put("wuziChuruInoutUuidNumber",wuziChuruInoutUuidNumber);
                                }
                        }

                        //查询是否重复
                         //出入库流水号
                        List<WuziChuruInoutEntity> wuziChuruInoutEntities_wuziChuruInoutUuidNumber = wuziChuruInoutService.selectList(new EntityWrapper<WuziChuruInoutEntity>().in("wuzi_churu_inout_uuid_number", seachFields.get("wuziChuruInoutUuidNumber")));
                        if(wuziChuruInoutEntities_wuziChuruInoutUuidNumber.size() >0 ){
                            ArrayList<String> repeatFields = new ArrayList<>();
                            for(WuziChuruInoutEntity s:wuziChuruInoutEntities_wuziChuruInoutUuidNumber){
                                repeatFields.add(s.getWuziChuruInoutUuidNumber());
                            }
                            return R.error(511,"数据库的该表中的 [出入库流水号] 字段已经存在 存在数据为:"+repeatFields.toString());
                        }
                        wuziChuruInoutService.insertBatch(wuziChuruInoutList);
                        return R.ok();
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            return R.error(511,"批量插入数据异常，请联系管理员");
        }
    }




    /**
    * 前端列表
    */
    @IgnoreAuth
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params, HttpServletRequest request){
        logger.debug("list方法:,,Controller:{},,params:{}",this.getClass().getName(),JSONObject.toJSONString(params));

        CommonUtil.checkMap(params);
        PageUtils page = wuziChuruInoutService.queryPage(params);

        //字典表数据转换
        List<WuziChuruInoutView> list =(List<WuziChuruInoutView>)page.getList();
        for(WuziChuruInoutView c:list)
            dictionaryService.dictionaryConvert(c, request); //修改对应字典表字段

        return R.ok().put("data", page);
    }

    /**
    * 前端详情
    */
    @RequestMapping("/detail/{id}")
    public R detail(@PathVariable("id") Long id, HttpServletRequest request){
        logger.debug("detail方法:,,Controller:{},,id:{}",this.getClass().getName(),id);
        WuziChuruInoutEntity wuziChuruInout = wuziChuruInoutService.selectById(id);
            if(wuziChuruInout !=null){


                //entity转view
                WuziChuruInoutView view = new WuziChuruInoutView();
                BeanUtils.copyProperties( wuziChuruInout , view );//把实体数据重构到view中

                //修改对应字典表字段
                dictionaryService.dictionaryConvert(view, request);
                return R.ok().put("data", view);
            }else {
                return R.error(511,"查不到数据");
            }
    }


    /**
    * 前端保存
    */
    @RequestMapping("/add")
    public R add(@RequestBody WuziChuruInoutEntity wuziChuruInout, HttpServletRequest request){
        logger.debug("add方法:,,Controller:{},,wuziChuruInout:{}",this.getClass().getName(),wuziChuruInout.toString());
        Wrapper<WuziChuruInoutEntity> queryWrapper = new EntityWrapper<WuziChuruInoutEntity>()
            .eq("wuzi_churu_inout_uuid_number", wuziChuruInout.getWuziChuruInoutUuidNumber())
            .eq("wuzi_churu_inout_types", wuziChuruInout.getWuziChuruInoutTypes())
//            .notIn("wuzi_churu_inout_types", new Integer[]{102})
            ;
        logger.info("sql语句:"+queryWrapper.getSqlSegment());
        WuziChuruInoutEntity wuziChuruInoutEntity = wuziChuruInoutService.selectOne(queryWrapper);
        if(wuziChuruInoutEntity==null){
            wuziChuruInout.setInsertTime(new Date());
            wuziChuruInout.setCreateTime(new Date());
        wuziChuruInoutService.insert(wuziChuruInout);

            return R.ok();
        }else {
            return R.error(511,"表中有相同数据");
        }
    }

}

