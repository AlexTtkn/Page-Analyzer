@import hexlet.code.dto.urls.UrlPage
@param UrlPage page

@template.layout.page(
page = page,
content = @`
    <div class="container-lg mt-5">
        <h2>Page ${page.getName()}</h2>
        <table class="table mt-3">
            <tbody>
            <tr>
                <th scope="row">ID</th>
                <td>${page.getId()}</td>
            </tr>
            <tr>
                <th scope="row">Name</th>
                <td>${page.getName()}</td>
            </tr>
            <tr>
                <th scope="row">Created at</th>
                <td>${String.valueOf(page.getCreatedAt())}</td>
            </tr>
            </tbody>
        </table>
    </div>
`
)