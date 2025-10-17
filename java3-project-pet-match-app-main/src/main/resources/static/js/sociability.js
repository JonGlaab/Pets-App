document.addEventListener("DOMContentLoaded", function () {
    const exclusivityMap = {
        "LIKES_CATS": "DISLIKES_CATS",
        "DISLIKES_CATS": "LIKES_CATS",
        "LIKES_DOGS": "DISLIKES_DOGS",
        "DISLIKES_DOGS": "LIKES_DOGS",
        "LIKE_KIDS": "DISLIKES_KIDS",
        "DISLIKES_KIDS": "LIKE_KIDS"
    };

    Object.keys(exclusivityMap).forEach(id => {
        const checkbox = document.getElementById(id);
        const oppositeId = exclusivityMap[id];
        const oppositeCheckbox = document.getElementById(oppositeId);

        if (checkbox && oppositeCheckbox) {
            checkbox.addEventListener("change", function () {
                if (checkbox.checked) {
                    oppositeCheckbox.checked = false;
                    oppositeCheckbox.disabled = true;
                } else {
                    oppositeCheckbox.disabled = false;
                }
            });
        }
    });
});