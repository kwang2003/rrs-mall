package com.aixforce.admin.web.controller;

import com.aixforce.common.model.Response;
import com.aixforce.exception.JsonResponseException;
import com.aixforce.user.base.BaseUser;
import com.aixforce.user.base.UserUtil;
import com.aixforce.web.misc.MessageSources;
import com.rrs.brand.model.BrandClub;
import com.rrs.brand.model.BrandUserAnnouncement;
import com.rrs.brand.service.BrandClubService;
import com.rrs.brand.service.BrandUserAnnouncementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Created by zhum01 on 2014/7/25.
 */

@Slf4j
@Controller
@RequestMapping("/brandUserAnn")
public class BrandUserAnnController {

    @Autowired
    private BrandUserAnnouncementService brandUserAnnouncementService;

    @Autowired
    private MessageSources messageSources;

    @RequestMapping(value = "/queryAll", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Response<List<BrandUserAnnouncement>> queryAll() {
        BaseUser baseUser= UserUtil.getCurrentUser();
        Response<List<BrandUserAnnouncement>> result = brandUserAnnouncementService.findAll(baseUser);
        return result;
    }

    @RequestMapping(value = "/queryByTime", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<BrandUserAnnouncement> queryByTime(@RequestParam("starttime") String starttime,@RequestParam("endtime") String endtime) {
        List<BrandUserAnnouncement> result = brandUserAnnouncementService.findByTime(starttime,endtime);
        return result;
    }

    @RequestMapping(value = "/queryById", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public BrandUserAnnouncement queryById(@RequestParam("id") int id) {
        BrandUserAnnouncement result = brandUserAnnouncementService.findById(id);
        return result;
    }

    @RequestMapping(value = "/addAnn", method = RequestMethod.GET)
    public String addAnn(@RequestParam("announcement") String announcement,@RequestParam("status") int status,@RequestParam("title") String title) {
        BaseUser baseUser= UserUtil.getCurrentUser();
        BrandUserAnnouncement bu=new BrandUserAnnouncement();
        bu.setAnnouncement(announcement);
        bu.setTitle(title);
        bu.setStatus(status);
        brandUserAnnouncementService.addAnn(bu,baseUser);
        return "redirect:/operations/brandAnn";
    }

    @RequestMapping(value = "/delAnn", method = RequestMethod.GET)
    public String delAnn(@RequestParam("idlist") String[] idlist) {
        if(idlist.length>0){
            int[] ia = new int[idlist.length];
            for(int i=0;i<idlist.length;i++){
                ia[i]=Integer.parseInt(idlist[i]);
            }
            brandUserAnnouncementService.delAnn(ia);
        }
        return "redirect:/operations/brandAnn";
    }


}
