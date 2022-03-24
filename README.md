# html-parser

This is a proof-of-concept for parsing T&C pages, and outputting the following:
- 1 HTML file with existing attributes preserved, play-frontend-hmrc styles added to elements, and innerHTML text replaced with messages API calls with keys (`OnlineTermsPage.txt`)
- 1 messages file containing the English language text, with keys matching the HTML file also produced (`messages`)
- 1 messages-cy file containing the Welsh language text, with keys matching the HTML file also produced(`messages-cy`)

## Useage
To use the parser:
- Add the HTML from within the `<main>` element of the **English language** version of the page to the root directory as `page-source-en.txt`
- Add the HTML from within the `<main>` element of the **Welsh language** version of the page to the root directory as `page-source-cy.txt`
- Run the command `sbt run`
