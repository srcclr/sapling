# Deploying the [front end](https://github.com/srcclr/sapling-frontend)

The front end is a client-side rendered SPA, so there are many options for deploying it. We went with Netlify.

First create a site on Netlify via the web app, then take note of the API ID (under Settings). Also generate a personal access token (under User Settings). These go in the GitHub secrets `NETLIFY_AUTH_TOKEN` and `NETLIFY_SITE_ID`.

[Redirect rules](https://www.netlify.com/blog/2019/01/16/redirect-rules-for-all-how-to-configure-redirects-for-your-static-site/) are also [required](https://github.com/srcclr/sapling-frontend/blob/master/.github/actions/build/Dockerfile) for the app to work correctly.

That's about it. Check out the [workflow configuration](https://github.com/srcclr/sapling-frontend/blob/master/.github/workflows/main.yml) for the details.

The final step is to add the domain to the CORS whitelist:

```sh
heroku config:set PROPS_ADDITIONAL_ALLOWED_ORIGINS="https://your-site.netlify.com" -a sapling-planning
```
