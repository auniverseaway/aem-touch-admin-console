(function(document, $) {
    "use strict";

    var URL_BASE =  "/apps/pugranch/admin/ext/uptime/content";

    var MAIN_PAGE_PATH = URL_BASE + "/admin.html";
    var EDIT_PAGE_PATH = URL_BASE + "/editEntry.html"
    
    var ui = $(window).adaptTo("foundation-ui");

    $(document).on("foundation-selections-change", ".granite-uptime-collection", function() {
        var deleteButton = $("#toggleDeleteEntryDialog");
        var editButton = $("#editEntryDialog");
    });

    $(document).on("click", "#toggleDeleteEntryDialog", function(e) {
        var entryName = $(".foundation-selections-item").data("id");
        var dialog = document.querySelector("#deleteEntryDialog");
        if(!dialog) {
            dialog = new Coral.Dialog().set({
                variant: "warning",
                id: "deleteEntryDialog",
                header: {
                    innerHTML: Granite.I18n.get("Delete Uptime Entry")
                },
                content: {
                    innerHTML: Granite.I18n.get("Are you sure you want to entry {0}?", entryName, "0 is the path of the entry to delete")
                },
                footer: {
                    innerHTML: "<button is='coral-button' variant='default' coral-close>" + Granite.I18n.get("No") +
                    "</button><button id='deleteEntryButton' is='coral-button' variant='primary'>" + Granite.I18n.get("Yes") + "</button>"
                }
            });
            document.body.appendChild(dialog);
        } else {
            dialog.content.innerHTML =
                Granite.I18n.get("Are you sure you want to delete entry {0}?", entryName, "0 is the path of the entry to delete");
        }
        dialog.show();
    });

    $(document).on("click", "#deleteEntryButton", function(e) {
        var selectedItem = $(".foundation-selections-item");
        var selectedItemPath = $(".foundation-selections-item").data("path");
        var dialog = document.querySelector("#deleteEntryDialog");
        var switcherItem = $(".foundation-mode-switcher-item");
        $.ajax({
            url: Granite.HTTP.externalize(selectedItemPath),
            type: "POST",
            data: {
                ":operation": "delete"
            },
            success: function() {
                selectedItem.remove();
                if(dialog) {
                    dialog.hide();
                }
                if(switcherItem) {
                    switcherItem.removeClass('foundation-mode-switcher-item-active');
                }
            },
            error: function(xmlhttprequest, textStatus, message) {
                if(dialog) {
                    dialog.hide();
                }
                ui.notify(Granite.I18n.get("Error"), Granite.I18n.get("An error occurred while deleting the entry: {0}.", message, "0 is the error message"), "error")
            }
        });
    });

    $(document).on("click", "#editEntryDialog", function(e) {
        var selectedEntry = $(".foundation-selections-item").data("path");
        window.location.href = Granite.HTTP.externalize(EDIT_PAGE_PATH) + "?path=" + selectedEntry;
    });
})(document, Granite.$);
