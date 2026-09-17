package ir.ac.sutech.attendance_system.controller;

import ir.ac.sutech.attendance_system.dto.UserAccountForm;
import ir.ac.sutech.attendance_system.model.Role;
import ir.ac.sutech.attendance_system.service.UserAccountService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/users")
public class UserAccountController {

    private final UserAccountService userAccountService;

    public UserAccountController(
            UserAccountService userAccountService
    ) {
        this.userAccountService = userAccountService;
    }

    @GetMapping
    public String listAccounts(Model model) {
        model.addAttribute(
                "accounts",
                userAccountService.findAllAccounts()
        );

        model.addAttribute(
                "accountCount",
                userAccountService.countAccounts()
        );

        return "admin/users/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        UserAccountForm form = new UserAccountForm();
        form.setActive(true);

        prepareFormPage(
                model,
                form,
                null,
                false
        );

        return "admin/users/form";
    }

    @PostMapping
    public String createAccount(
            @Valid
            @ModelAttribute("form") UserAccountForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            prepareFormPage(
                    model,
                    form,
                    null,
                    false
            );

            return "admin/users/form";
        }

        try {
            userAccountService.createAccount(form);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "حساب کاربری با موفقیت ساخته شد"
            );

            return "redirect:/admin/users";

        } catch (IllegalArgumentException exception) {
            bindingResult.reject(
                    "account.create.failed",
                    exception.getMessage()
            );

            prepareFormPage(
                    model,
                    form,
                    null,
                    false
            );

            return "admin/users/form";
        }
    }

    @GetMapping("/{accountId}/edit")
    public String showEditForm(
            @PathVariable Long accountId,
            Model model
    ) {
        UserAccountForm form =
                userAccountService.findFormById(accountId);

        prepareFormPage(
                model,
                form,
                accountId,
                true
        );

        return "admin/users/form";
    }

    @PostMapping("/{accountId}")
    public String updateAccount(
            @PathVariable Long accountId,
            @Valid
            @ModelAttribute("form") UserAccountForm form,
            BindingResult bindingResult,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            prepareFormPage(
                    model,
                    form,
                    accountId,
                    true
            );

            return "admin/users/form";
        }

        try {
            userAccountService.updateAccount(
                    accountId,
                    form,
                    authentication.getName()
            );

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "اطلاعات حساب کاربری ویرایش شد"
            );

            return "redirect:/admin/users";

        } catch (IllegalArgumentException exception) {
            bindingResult.reject(
                    "account.update.failed",
                    exception.getMessage()
            );

            prepareFormPage(
                    model,
                    form,
                    accountId,
                    true
            );

            return "admin/users/form";
        }
    }

    @PostMapping("/{accountId}/status")
    public String updateStatus(
            @PathVariable Long accountId,
            @RequestParam boolean active,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        try {
            userAccountService.updateStatus(
                    accountId,
                    active,
                    authentication.getName()
            );

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    active
                            ? "حساب کاربری فعال شد"
                            : "حساب کاربری غیرفعال شد"
            );

        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    exception.getMessage()
            );
        }

        return "redirect:/admin/users";
    }

    private void prepareFormPage(
            Model model,
            UserAccountForm form,
            Long accountId,
            boolean editing
    ) {
        model.addAttribute("form", form);
        model.addAttribute("accountId", accountId);
        model.addAttribute("editing", editing);
        model.addAttribute("roles", Role.values());

        model.addAttribute(
                "employees",
                userAccountService.findActiveEmployees()
        );
    }
}