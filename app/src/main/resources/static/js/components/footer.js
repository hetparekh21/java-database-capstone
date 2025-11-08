/*
  Function to render the footer content into the page
      /* footer.js
         Renders a consistent footer across pages.

         Responsibilities implemented:
         - renderFooter(): injects footer HTML into element with id="footer"
         - Provides accessible logo and grouped links (Company, Support, Legals)
         - Shows current year dynamically
         - Exposes renderFooter globally and automatically runs it on DOMContentLoaded
      */

(function () {
  "use strict";

  function getFooterDiv() {
    return document.getElementById("footer");
  }

  function buildFooterHTML() {
    const year = new Date().getFullYear();
    return `
            <footer class="footer">
              <div class="footer-container">
                <div class="footer-logo">
                  <img src="../assets/images/logo/logo.png" alt="Hospital CMS Logo" class="footer-logo-img">
                  <p>© Copyright ${year}. All Rights Reserved by Hospital CMS.</p>
                </div>

                <div class="footer-links">
                  <div class="footer-column">
                    <h4>Company</h4>
                    <a href="/pages/about.html">About</a>
                    <a href="#">Careers</a>
                    <a href="#">Press</a>
                  </div>

                  <div class="footer-column">
                    <h4>Support</h4>
                    <a href="#">Account</a>
                    <a href="#">Help Center</a>
                    <a href="/pages/contact.html">Contact Us</a>
                  </div>

                  <div class="footer-column">
                    <h4>Legals</h4>
                    <a href="/pages/terms.html">Terms &amp; Conditions</a>
                    <a href="/pages/privacy.html">Privacy Policy</a>
                    <a href="#">Licensing</a>
                  </div>
                </div>
              </div>
            </footer>`;
  }

  function renderFooter() {
    const footerDiv = getFooterDiv();
    if (!footerDiv) return;
    footerDiv.innerHTML = buildFooterHTML();

    // Accessibility: ensure links open in same tab except external ones
    const links = footerDiv.querySelectorAll("a");
    links.forEach((a) => {
      // add rel for security if external
      try {
        const href = a.getAttribute("href");
        if (href && href.startsWith("http")) {
          a.setAttribute("rel", "noopener noreferrer");
          a.setAttribute("target", "_blank");
        }
      } catch (e) {
        // ignore
      }
    });
  }

  // expose and auto-render
  window.renderFooter = renderFooter;
  document.addEventListener("DOMContentLoaded", renderFooter);
})();
