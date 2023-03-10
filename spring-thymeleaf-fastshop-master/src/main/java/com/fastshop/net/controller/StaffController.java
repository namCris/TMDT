package com.fastshop.net.controller;

import java.time.LocalDate;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.fastshop.net.model.Account;
import com.fastshop.net.model.Authority;
import com.fastshop.net.model.Category;
import com.fastshop.net.model.MailInfo;
import com.fastshop.net.model.Order;
import com.fastshop.net.model.Product;
import com.fastshop.net.model.ProductDTO;
import com.fastshop.net.model.ProductDetail;
import com.fastshop.net.model.Status;
import com.fastshop.net.service.ProductService;
import com.fastshop.net.service.StatusService;
import com.fastshop.net.service._CookieService;
import com.fastshop.net.service._GetListFile;
import com.fastshop.net.service._MailService;
import com.fastshop.net.utils.FormatDate;
import com.fastshop.net.service.AccountService;
import com.fastshop.net.service.AuthorityService;
import com.fastshop.net.service.CategoryDetailService;
import com.fastshop.net.service.CategoryService;
import com.fastshop.net.service.DiscountService;
import com.fastshop.net.service.NotifyService;
import com.fastshop.net.service.OrderService;
import com.fastshop.net.service.ProductDetailService;

@Controller
public class StaffController {
    @Autowired
    _CookieService cookie;
    @Autowired
    _GetListFile getListFile;
    @Autowired
    _MailService mailService;
    @Autowired
    AuthorityService authorityService;
    @Autowired
    AccountService accountService;
    @Autowired
    ProductService productSevice;
    @Autowired
    CategoryService categoryService;
    @Autowired
    CategoryDetailService categoryDetailService;
    @Autowired
    ProductDetailService productDetailService;
    @Autowired
    OrderService orderService;
    @Autowired
    StatusService statusService;
    @Autowired
    DiscountService discountService;
    @Autowired
    NotifyService notifyService;
    
    // trang chu c???a order c??ng l?? trang c?? danh s??ch l?? ti???p nh???n
    @RequestMapping("/staff/home")
    public String home(Model model, @ModelAttribute("auth") Authority auth) {
        try {
            return "redirect:/staff/orders/status/0";
        } catch (Exception e) {
            return "redirect:/login.fastshop.com";
        }
        
    }

    // trang qu???n l?? th??ng tin chi ti???t c???a product
    @RequestMapping("/staff/product")
    public String products(Model model, @ModelAttribute("auth") Authority auth) {
        try {
            model.addAttribute("items", productSevice.findAll());
            model.addAttribute("page", "staff.product");
            model.addAttribute("title_main", "Th??ng tin chi ti???t c??c s???n ph???m");
            model.addAttribute("count_notify", notifyService.findAllByAccAndNowAndStatusTrueOrderBy(auth.getAccount()));
            return "index";
        } catch (Exception e) {
            return "redirect:/login.fastshop.com";
        }
    }

    // trang th??m s???a x??a d??? li???u product
    @RequestMapping("/staff/products")
    public String product(Model model, @ModelAttribute("auth") Authority authority) {
        try {
            String title_main = "User - Qu???n l?? s???n ph???m";
            model.addAttribute("page", "staff.products");
            model.addAttribute("product", new Product());
            model.addAttribute("productDTO", new ProductDTO());
            model.addAttribute("title_main", title_main);
            model.addAttribute("categories", categoryService.findAll());
            model.addAttribute("productAll", productSevice.findAll());
            model.addAttribute("voucherAll", discountService.findByAll());
            model.addAttribute("count_notify", notifyService.findAllByAccAndNowAndStatusTrueOrderBy(authority.getAccount()));

            return "index";
        } catch (Exception e) {
            return "redirect:/login.fastshop.com";
        }
    }

    // trang c???a category v???i 3 tr???ng th??i
    @RequestMapping("/staff/category/{status}")
    public String category(Model model, @PathVariable("status") String status, @ModelAttribute("auth") Authority authority) {
        try {
            model.addAttribute("page", "staff.category");
            model.addAttribute("category", new Category());
            model.addAttribute("count_notify", notifyService.findAllByAccAndNowAndStatusTrueOrderBy(authority.getAccount()));
            String title_main = "";

            if (status.equals("stock")) {
                title_main = "Staff - Danh s??ch ph??n lo???i c??n b??n";
                model.addAttribute("focus", "stock");
                model.addAttribute("title", "LO???I H??NG C??N HO???T ?????NG");
                model.addAttribute("title_main", title_main);
                model.addAttribute("categories", categoryService.findByStatus(true));
            }
            else if (status.equals("out")) {
                title_main = "Staff - Danh s??ch ph??n lo???i h???t h??ng";
                model.addAttribute("focus", "out");
                model.addAttribute("title", "LO???I H??NG ???? T???M D???NG");
                model.addAttribute("title_main", title_main);
                model.addAttribute("categories", categoryService.findByStatus(false));
            } 
            else {
                title_main = "Staff - Danh s??ch t???t c??? ph??n lo???i";
                model.addAttribute("focus", "all");
                model.addAttribute("title", "T???T C??? LO???I H??NG");
                model.addAttribute("title_main", title_main);
                model.addAttribute("categories", categoryService.findAll());
            }
            return "index";
        } catch (Exception e) {
            return "redirect:/login.fastshop.com";
        }
    }


    // trang vi???t report
    @RequestMapping("/staff/report")
    public String report(Model model, @ModelAttribute("auth") Authority auth) {
        try {
            Account account = auth.getAccount();
            model.addAttribute("page", "staff.report");
            model.addAttribute("now", FormatDate.parse());
            model.addAttribute("title_main", "B??o c??o ho???ch to??n h???ng ng??y");
            model.addAttribute("products", productSevice.findAll());
            model.addAttribute("count_notify", notifyService.findAllByAccAndNowAndStatusTrueOrderBy(account));
            return "index";
        } catch (Exception e) {
            return "redirect:/login.fastshop.com";
        }
    }


    // function nhan nut xac nhan
    @RequestMapping("/staff/confirm/{id}")
    public String confirm(Model model, @RequestParam("status") Integer kind, @PathVariable("id") Long orderId, @ModelAttribute("mailInfo") MailInfo mailInfo) {
        try {
            Order order = orderService.findById(orderId);
            Status status = statusService.findById(kind);
            order.setStatus(status);
            order.setDateConfirm(new Date());
            orderService.save(order);
            if (kind == -1) {
                mailService.send(mailInfo);                
            }
            return "redirect:/staff/orders/status/" + (kind - 1);
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/login.fastshop.com";
        }
    }

    // x??? l?? th??ng tin chi ti???t cho 1 product trong danh s??ch products
    @RequestMapping("/staff/detail/{id}")
    public String details(Model model, @ModelAttribute("auth") Authority auth, @PathVariable("id") Integer id) {
        try {
            Product product = productSevice.findById(id);
            model.addAttribute("product", product);
            model.addAttribute("productDetail", new ProductDetail());
            model.addAttribute("page", "staff.detail");
            model.addAttribute("productdetails", productDetailService.getByProductId(id));
            model.addAttribute("count_notify", notifyService.findAllByAccAndNowAndStatusTrueOrderBy(auth.getAccount()));
            model.addAttribute("title_main", "Form th??ng tin chi ti???t m?? t??? s???n ph???m");
            return "index";
        } catch (Exception e) {
            return "redirect:/login.fastshop.com";
        }
    }

    // th??m m?? t??? cho 1 product
    @RequestMapping("/staff/describe/{id}")
    public String change(Model model, @RequestParam("describe") String describe, @PathVariable("id") Integer id) {
        try {
            Product product = productSevice.findById(id);
            product.setDescribe(describe);
            productSevice.save(product);
            model.addAttribute("page", "staff.detail");
            return "redirect:/staff/detail/" + id;
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/login.fastshop.com";
        }
    }

    // qu???n l?? discount
    @RequestMapping("/staff/discount")
    public String dicsount(Model model, @ModelAttribute("auth") Authority auth) {
        try {
            model.addAttribute("page", "staff.discount");
            model.addAttribute("count_notify", notifyService.findAllByAccAndNowAndStatusTrueOrderBy(auth.getAccount()));
            model.addAttribute("title_main", "Fastshop - Gi???m gi?? s???n ph???m");
            return "index";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/login.fastshop.com";
        }
    }

    // c???p nh???t l???i n???i dung c???a describe
    @RequestMapping("/staff/redetail/{id}")
    public String redetail(Model model, @RequestParam("describe") String describe, @PathVariable("id") Integer id) {
        try {
            model.addAttribute("page", "staff.detail");
            return "redirect:/staff/detail/" + id;
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/login.fastshop.com";
        }
    }


    // l???y danh s??ch ????n h??ng theo 5 tr???ng th??i ti???p nh???n, ?????ng g??i, ??ang giao, th??nh c??ng, h???y
    @RequestMapping("/staff/orders/status/{id}")
    public String status(Model model, @ModelAttribute("auth") Authority auth, @PathVariable("id") Integer status) {
        try {
            model.addAttribute("mailInfo", new MailInfo(auth.getAccount().getEmail(), "Th??ng b??o ????n h??ng c???a b???n ???? b??? h???y t??? Fashshop.com", ""));
            Date toNow = java.sql.Date.valueOf(LocalDate.now());
            model.addAttribute("page", "staff.home");
            model.addAttribute("orderNotToday", orderService.findNotByCreateDate(toNow));
            model.addAttribute("status", status);
            model.addAttribute("title_main", "Danh s??ch h??a ????n theo lo???i");
            model.addAttribute("ordertoday", orderService.findByStatus(status));
            model.addAttribute("count_notify", notifyService.findAllByAccAndNowAndStatusTrueOrderBy(auth.getAccount()));
            return "index";
        } catch (Exception e) {
            return "redirect:/login.fastshop.com";
        }
    }


    /**
     * this is model of authority
     * @return
     */
    @ModelAttribute("auth")
    public Authority getAuth() {
        Authority auth = null;
        String username = cookie.getValue("username");
        if (username != null) {
            Account account = accountService.findByUsernameOrEmail(username, username);
            auth = authorityService.findByAccount(account);
        }
        return auth;
    }
}
